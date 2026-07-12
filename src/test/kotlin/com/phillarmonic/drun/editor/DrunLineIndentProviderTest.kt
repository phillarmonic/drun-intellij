package com.phillarmonic.drun.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class DrunLineIndentProviderTest {
    @Test fun `indents after colon block`() {
        val text = "task \"build\":\n"
        assertEquals("  ", DrunLineIndentProvider.calculateIndent(text, text.length))
    }

    @Test fun `keeps indentation after ordinary line`() {
        val text = "task \"build\":\n  step \"one\"\n"
        assertEquals("  ", DrunLineIndentProvider.calculateIndent(text, text.length))
    }

    @Test fun `dedents branch clauses`() {
        val text = "if docker is available:\n  step \"yes\"\n  else:"
        assertEquals("", DrunLineIndentProvider.calculateIndent(text, text.length))
    }
}
