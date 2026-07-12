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
