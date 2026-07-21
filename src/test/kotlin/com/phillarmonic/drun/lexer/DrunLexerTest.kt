package com.phillarmonic.drun.lexer

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.phillarmonic.drun.highlight.DrunSyntaxHighlighter
import com.phillarmonic.drun.highlight.DrunTextAttributes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrunLexerTest {
    @Test fun `recognizes both warning action spellings`() {
        val tokens = lex("""warn "Short warning spelling"
warning "Full warning spelling"""")

        assertHas(tokens, DrunTokenTypes.ACTION, "warn")
        assertHas(tokens, DrunTokenTypes.ACTION, "warning")
    }

    @Test fun `recognizes representative language families`() {
        val tokens = lex("""@platform("linux")
task "deploy":
  given ${'$'}environment defaults to "staging"
  if docker is available:
    build docker image "app:{${'$'}environment}"
    success "done" # comment
""")
        assertHas(tokens, DrunTokenTypes.ANNOTATION, "@platform")
        assertHas(tokens, DrunTokenTypes.DEFINITION, "\"deploy\"")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}environment")
        assertHas(tokens, DrunTokenTypes.KEYWORD, "if")
        assertHas(tokens, DrunTokenTypes.TYPE, "docker")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}environment")
        assertHas(tokens, DrunTokenTypes.LINE_COMMENT, "# comment")
    }

    @Test fun `recovers after strings and block comments`() {
        val tokens = lex("\"escaped \\\" value\" /* multi\nline */ task next")
        assertHas(tokens, DrunTokenTypes.STRING_ESCAPE, "\\\"")
        assertHas(tokens, DrunTokenTypes.BLOCK_COMMENT, "/* multi\nline */")
        assertHas(tokens, DrunTokenTypes.DEFINITION, "next")
    }

    @Test fun `recognizes interpolation variants inside strings`() {
        val tokens = lex("""info "plain {port}, explicit {${'$'}version}, dotted {service.port}"""")
        assertEquals(3, tokens.count { it.first == DrunTokenTypes.INTERPOLATION && it.second == "{" })
        assertEquals(3, tokens.count { it.first == DrunTokenTypes.INTERPOLATION && it.second == "}" })
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "port")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}version")
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "service.port")
    }

    @Test fun `highlights expressions and macro calls inside interpolations`() {
        val tokens = lex("""set ${'$'}release_version to "{${'$'}version without prefix 'v'}"
info "{if ${'$'}environment is 'production' then 'prod' else 'dev'}"
info "{secret('api_key')}"
""")

        assertEquals("interpolation expressions must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        listOf("${'$'}version", "${'$'}environment").forEach {
            assertHas(tokens, DrunTokenTypes.VARIABLE, it)
        }
        listOf("without", "if", "is", "then", "else").forEach {
            assertHas(tokens, DrunTokenTypes.KEYWORD, it)
        }
        listOf("prefix", "secret").forEach {
            assertHas(tokens, DrunTokenTypes.MACRO, it)
        }
        listOf("v", "production", "prod", "dev", "api_key").forEach {
            assertHas(tokens, DrunTokenTypes.STRING, it)
        }
    }

    @Test fun `recognizes the unreleased upstream interpolation macro set`() {
        val operations = listOf(
            "concat", "split", "replace", "trim", "uppercase", "lowercase", "prepend", "join",
            "slice", "length", "keys", "values", "basename", "dirname", "extension", "prefix",
            "suffix", "filtered", "sorted", "reversed", "unique", "first", "last",
        )
        val builtins = listOf(
            "current git commit", "current git branch", "available tasks", "file exists", "dir exists", "start progress",
            "update progress", "finish progress", "start timer", "stop timer", "show elapsed time",
            "docker compose command", "docker compose status", "compose_cmd", "dns_resolve", "dns_check",
            "dns_validate", "now", "pwd", "hostname", "env", "secret", "current",
        )
        val source = buildString {
            operations.forEach { appendLine("""info "{${'$'}value $it 'x'}"""") }
            builtins.forEach { appendLine("""info "{$it}"""") }
            appendLine("""info "{available tasks(', ', 'default', 'internal.release')}"""")
            appendLine("""info "{custom_macro('x')}"""")
            appendLine("""info "{${'$'}outer ? 'prefix-{${'$'}inner}' : ''}"""")
            appendLine("""run "echo ${'$'}{HOME:-/tmp}"""")
        }
        val tokens = lex(source)

        (operations + builtins + "custom_macro").forEach {
            assertHas(tokens, DrunTokenTypes.MACRO, it)
        }
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}inner")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}{HOME:-/tmp}")
        listOf(", ", "default", "internal.release").forEach {
            assertHas(tokens, DrunTokenTypes.STRING, it)
        }
        assertEquals("interpolation macros must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
    }

    @Test fun `recognizes variable names in declarations and expressions`() {
        val tokens = lex("given ${'$'}app_name defaults to \"myapp\"\nset ${'$'}replicas to 3")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}app_name")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}replicas")
    }

    @Test fun `styles word operators as keywords`() {
        val tokens = lex("""for ${'$'}i in range 1 to 3 in parallel:
if ${'$'}candidate is older than version "{${'$'}latest}":
  info "stale"
""")
        assertHas(tokens, DrunTokenTypes.KEYWORD, "range")
        assertHas(tokens, DrunTokenTypes.KEYWORD, "parallel")
        listOf("is", "older", "than").forEach { assertHas(tokens, DrunTokenTypes.KEYWORD, it) }
    }

    @Test fun `highlights comparison and logic symbols separately from arithmetic`() {
        val tokens = lex("if 2 >= 1 && 1 != 0 || !false:\n  set ${'$'}value to 1 + 2")
        listOf(">=", "&&", "!=", "||", "!").forEach {
            assertHas(tokens, DrunTokenTypes.LOGIC_OPERATOR, it)
        }
        assertHas(tokens, DrunTokenTypes.OPERATOR, "+")
    }

    @Test fun `highlights dependency task references`() {
        val tokens = lex("""depends on install
depends on lint and unit_tests
depends on integration_tests, security_scan
depends on ["database", "redis"]
depends on build then smoke_test in parallel
""")
        listOf("install", "lint", "unit_tests", "integration_tests", "security_scan", "build", "smoke_test", "\"database\"", "\"redis\"").forEach {
            assertHas(tokens, DrunTokenTypes.DEFINITION, it)
        }
        listOf("on", "and", "then", "in", "parallel").forEach {
            assertHas(tokens, DrunTokenTypes.KEYWORD, it)
        }
    }

    @Test fun `highlights required tools with and without version constraints`() {
        val tokens = lex("""project "aekf" version "1.0":
  requires tools:
    go >= "1.26"
    gosec
    govulncheck
    golangci-lint
    custom-tool >= 2.12 <= "3.0" provision
  info "project ready"

task "default":
  requires tools:
    task-tool
  info "task ready"
""")
        listOf("go", "gosec", "govulncheck", "golangci-lint", "custom-tool", "task-tool").forEach {
            assertHas(tokens, DrunTokenTypes.CONSTANT, it)
        }
        assertHas(tokens, DrunTokenTypes.LOGIC_OPERATOR, ">=")
        assertHas(tokens, DrunTokenTypes.STRING, "1.26")
        assertHas(tokens, DrunTokenTypes.NUMBER, "2.12")
        assertHas(tokens, DrunTokenTypes.CONSTANT, "provision")
        assertHas(tokens, DrunTokenTypes.KEYWORD, "task")
        assertHas(tokens, DrunTokenTypes.ACTION, "info")
    }

    @Test fun `highlights inherited required tool task refs`() {
        val tokens = lex("""project "qol-syntax" version "1.0.0":
  requires tools:
    go >= "1.23"
    from tasks:
      build
      "integration test"

task "build":
  requires tools:
    from tasks:
      "integration test"
    golangci-lint >= "2.12"
""")

        assertEquals("requires tools inheritance must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        assertEquals(2, tokens.count { it.first == DrunTokenTypes.KEYWORD && it.second == "from" })
        assertEquals(2, tokens.count { it.first == DrunTokenTypes.CONSTANT && it.second == "tasks" })
        assertHas(tokens, DrunTokenTypes.DEFINITION, "build")
        assertEquals(2, tokens.count { it.first == DrunTokenTypes.DEFINITION && it.second == "\"integration test\"" })
        assertHas(tokens, DrunTokenTypes.CONSTANT, "golangci-lint")
    }

    @Test fun `recognizes built in types in parameter declarations`() {
        val tokens = lex("""accepts ${'$'}items as list
given ${'$'}name as string
given ${'$'}config as json
requires ${'$'}version as string matching semver_optional_v
""")
        listOf("list", "string", "json").forEach {
            assertHas(tokens, DrunTokenTypes.TYPE, it)
        }
        assertHas(tokens, DrunTokenTypes.MACRO, "semver_optional_v")
        assertEquals(listOf(DrunTextAttributes.TYPE),
            DrunSyntaxHighlighter().getTokenHighlights(DrunTokenTypes.TYPE).toList())
        assertEquals(listOf(DrunTextAttributes.MACRO),
            DrunSyntaxHighlighter().getTokenHighlights(DrunTokenTypes.MACRO).toList())
    }

    @Test fun `highlights project configuration properties`() {
        val tokens = lex("""shell config:
  darwin:
    executable: "/bin/zsh"
    args:
      - "-l"
    environment:
      TERM: xterm-256color
      TEST_VAR: "hello"
""")
        listOf("executable", "TERM", "TEST_VAR").forEach {
            assertHas(tokens, DrunTokenTypes.PROPERTY, it)
        }
        listOf("darwin", "args", "environment").forEach {
            assertHas(tokens, DrunTokenTypes.CONSTANT, it)
        }
    }

    @Test fun `keeps multiline commands in string state`() {
        val source = """command "docker build \
  --build-arg ENV={${'$'}environment} \
  --build-arg VERSION={${'$'}version} \
  ."
command "echo 'Building {${'$'}version}'
go test ./...
echo 'Done'"
"""
        val tokens = lex(source)
        assertEquals("multiline command must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}environment")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}version")
        assertTrue("continued command text should remain a string",
            tokens.any { it.first == DrunTokenTypes.STRING && "go test ./..." in it.second })
    }

    @Test fun `recognizes policies detection and orchestration`() {
        val tokens = lex("""git policy:
  branch is attached
  branch:
    default branches: "main", "release"
    protected branches: "main", "release"
orchestration service is healthy""")
        assertEquals("git policy syntax must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        listOf("git", "policy", "orchestration", "service").forEach { word ->
            assertTrue("missing $word", tokens.any { it.second == word })
        }
        assertHas(tokens, DrunTokenTypes.KEYWORD, "is")
        assertHas(tokens, DrunTokenTypes.TYPE, "branch")
        listOf("default", "protected", "branches", "attached", "healthy").forEach { word ->
            assertHas(tokens, DrunTokenTypes.CONSTANT, word)
        }
    }

    @Test fun `highlights task metadata and workdir from project specs`() {
        val tokens = lex("""task "ci" mode "ci" means "Whole CI pipeline":
  call task test
  use workdir "docs"
  run "uv run zensical serve"
""")
        listOf("mode", "means", "call", "use").forEach { word ->
            assertHas(tokens, DrunTokenTypes.KEYWORD, word)
        }
        assertTrue("call task should highlight task as a sub-statement",
            tokens.any { it.first == DrunTokenTypes.SUB_STATEMENT && it.second == "task" })
        assertEquals(listOf(DrunTextAttributes.SUB_STATEMENT),
            DrunSyntaxHighlighter().getTokenHighlights(DrunTokenTypes.SUB_STATEMENT).toList())
        assertHas(tokens, DrunTokenTypes.CONSTANT, "workdir")
        assertHas(tokens, DrunTokenTypes.ACTION, "run")
    }

    @Test fun `highlights every file value statement family without bad characters`() {
        val source = """get property "pluginVersion" from "gradle.properties" as ${'$'}plugin_version
get json "/version" from "package.json" as ${'$'}package_version
get yaml "chart.appVersion" from "Chart.yaml" as ${'$'}chart_version
get toml "package.version" from "Cargo.toml" as ${'$'}crate_version
get match "(?m)^VERSION=(?P<value>[^\\r\\n]+)${'$'}" from "VERSION.txt" as ${'$'}version
check property "pluginVersion" in "gradle.properties" equals "2"
check json "/version" in "package.json" differs from "1"
check yaml "chart.version" in "Chart.yaml" equals "2"
check toml "package.version" in "Cargo.toml" differs from "1"
check match "(?P<value>.+)" in "VERSION" equals "2"
update property "pluginVersion" in "gradle.properties" to "2" or fail
update json "/version" in "package.json" to "2" or add as string
update yaml "chart.version" in "Chart.yaml" to "2" or add as number
update toml "package.prerelease" in "Cargo.toml" to "false" or add as boolean
update match "(?P<value>.+)" in "VERSION" to "2" or fail
"""
        val tokens = lex(source)

        assertEquals("file-value syntax must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        listOf("get", "check", "update").forEach { assertHas(tokens, DrunTokenTypes.ACTION, it) }
        listOf("property", "json", "yaml", "toml", "match").forEach {
            assertHas(tokens, DrunTokenTypes.TYPE, it)
        }
        listOf("equals", "differs").forEach { assertHas(tokens, DrunTokenTypes.WORD_COMPARISON, it) }
        assertEquals(listOf(DrunTextAttributes.WORD_COMPARISON),
            DrunSyntaxHighlighter().getTokenHighlights(DrunTokenTypes.WORD_COMPARISON).toList())
        listOf("from", "in", "to", "or", "as").forEach { assertHas(tokens, DrunTokenTypes.KEYWORD, it) }
        listOf("string", "number", "boolean").forEach { assertHas(tokens, DrunTokenTypes.TYPE, it) }
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}plugin_version")
    }

    @Test fun `highlights scm registries and version aware git queries`() {
        val source = """project "release":
  scm:
    git:
      github:
        drun-intellij:
          default: https
          cli:
            repository: "phillarmonic/drun-intellij"
      generic:
        php:
          filesystem: "../php-src"
          version tags: "php-{version}"
task "latest":
  git get latest version from php
    matching tags "php-{version}"
    in series "8.4"
    ordered by version
    as ${'$'}php_version
"""
        val tokens = lex(source)
        assertEquals("SCM syntax must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        assertHas(tokens, DrunTokenTypes.KEYWORD, "scm")
        assertHas(tokens, DrunTokenTypes.IDENTIFIER, "drun-intellij")
        assertEquals(listOf(DrunTextAttributes.DEFINITION),
            DrunSyntaxHighlighter().getTokenHighlights(DrunTokenTypes.IDENTIFIER).toList())
        listOf("github", "generic", "cli", "filesystem", "default", "latest", "tags", "series").forEach {
            assertHas(tokens, DrunTokenTypes.CONSTANT, it)
        }
        listOf("matching", "in", "ordered", "by", "as").forEach {
            assertHas(tokens, DrunTokenTypes.KEYWORD, it)
        }
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}php_version")
    }

    @Test fun `highlights single line and multiline git version guards`() {
        val source = """task "release":
  given ${'$'}candidate defaults to "8.4.24"
  git ensure ${'$'}candidate is newer than latest version from php
  git ensure ${'$'}candidate is newer than latest version from php
    using filesystem
    matching tags "php-{version}"
    as ${'$'}latest_version
"""
        val tokens = lex(source)
        assertEquals("Git guard syntax must not produce bad characters", emptyList<String>(),
            tokens.filter { it.first == DrunTokenTypes.BAD_CHARACTER }.map { it.second })
        assertHas(tokens, DrunTokenTypes.ACTION, "ensure")
        listOf("is", "newer", "than", "using", "matching", "as").forEach {
            assertHas(tokens, DrunTokenTypes.KEYWORD, it)
        }
        assertHas(tokens, DrunTokenTypes.TYPE, "git")
        listOf("latest", "filesystem", "tags").forEach {
            assertHas(tokens, DrunTokenTypes.CONSTANT, it)
        }
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}candidate")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}latest_version")
    }

    private fun lex(text: String): List<Pair<IElementType, String>> {
        val lexer = DrunLexerAdapter()
        lexer.start(text)
        val result = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) result += lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd)
            lexer.advance()
        }
        return result
    }

    private fun assertHas(tokens: List<Pair<IElementType, String>>, type: IElementType, text: String) =
        assertEquals("token '$text'", type, tokens.firstOrNull { it.second == text }?.first)
}
