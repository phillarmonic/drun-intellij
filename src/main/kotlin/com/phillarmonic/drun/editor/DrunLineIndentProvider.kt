package com.phillarmonic.drun.editor

import com.intellij.lang.Language
import com.intellij.application.options.CodeStyle
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider
import com.phillarmonic.drun.DrunLanguage

class DrunLineIndentProvider : LineIndentProvider {
    override fun isSuitableFor(language: Language?) = language?.isKindOf(DrunLanguage) == true

    override fun getLineIndent(project: Project, editor: Editor, language: Language?, offset: Int): String? {
        val indentSize = CodeStyle.getIndentOptions(project, editor.document).INDENT_SIZE.coerceAtLeast(1)
        return calculateIndent(editor.document.charsSequence, offset, indentSize)
    }

    companion object {
        private val branch = Regex("^(else(?:\\s+if)?|otherwise|catch|finally)\\b")

        internal fun calculateIndent(text: CharSequence, offset: Int, indentSize: Int = 2): String? {
            val before = text.subSequence(0, offset.coerceIn(0, text.length)).toString()
            val lines = before.split('\n')
            val current = lines.lastOrNull().orEmpty().trimStart()
            val previous = lines.dropLast(1).lastOrNull { it.isNotBlank() } ?: return ""
            val previousIndent = previous.takeWhile { it == ' ' || it == '\t' }
            var width = previousIndent.sumOf { if (it == '\t') indentSize else 1 }
            if (previous.trimEnd().endsWith(':')) width += indentSize
            if (branch.containsMatchIn(current)) width = (width - indentSize).coerceAtLeast(0)
            return " ".repeat(width)
        }
    }
}
