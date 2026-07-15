package com.phillarmonic.drun.lexer

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrunLexerTest {
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
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "{${'$'}environment}")
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
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "{port}")
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "{${'$'}version}")
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "{service.port}")
    }

    @Test fun `recognizes variable names in declarations and expressions`() {
        val tokens = lex("given ${'$'}app_name defaults to \"myapp\"\nset ${'$'}replicas to 3")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}app_name")
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}replicas")
    }

    @Test fun `styles word operators as keywords`() {
        val tokens = lex("for ${'$'}i in range 1 to 3 in parallel:")
        assertHas(tokens, DrunTokenTypes.KEYWORD, "range")
        assertHas(tokens, DrunTokenTypes.KEYWORD, "parallel")
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

    @Test fun `recognizes built in types in parameter declarations`() {
        val tokens = lex("""accepts ${'$'}items as list
given ${'$'}name as string
given ${'$'}config as json
""")
        listOf("list", "string", "json").forEach {
            assertHas(tokens, DrunTokenTypes.TYPE, it)
        }
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
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "{${'$'}environment}")
        assertHas(tokens, DrunTokenTypes.INTERPOLATION, "{${'$'}version}")
        assertTrue("continued command text should remain a string",
            tokens.any { it.first == DrunTokenTypes.STRING && "go test ./..." in it.second })
    }

    @Test fun `recognizes policies detection and orchestration`() {
        val tokens = lex("git policy:\n  branch is attached\norchestration service is healthy")
        listOf("git", "policy", "orchestration", "service").forEach { word ->
            assertTrue("missing $word", tokens.any { it.second == word })
        }
        assertHas(tokens, DrunTokenTypes.KEYWORD, "is")
        listOf("attached", "healthy").forEach { word -> assertHas(tokens, DrunTokenTypes.CONSTANT, word) }
    }

    @Test fun `highlights task metadata and workdir from project specs`() {
        val tokens = lex("""task "ci" mode "ci" means "Whole CI pipeline":
  call task test
  use workdir "docs"
  run "uv run zensical serve"
""")
        listOf("task", "mode", "means", "call", "use").forEach { word ->
            assertHas(tokens, DrunTokenTypes.KEYWORD, word)
        }
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
        listOf("equals", "differs").forEach { assertHas(tokens, DrunTokenTypes.LOGIC_OPERATOR, it) }
        listOf("from", "in", "to", "or", "as").forEach { assertHas(tokens, DrunTokenTypes.KEYWORD, it) }
        listOf("string", "number", "boolean").forEach { assertHas(tokens, DrunTokenTypes.TYPE, it) }
        assertHas(tokens, DrunTokenTypes.VARIABLE, "${'$'}plugin_version")
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
