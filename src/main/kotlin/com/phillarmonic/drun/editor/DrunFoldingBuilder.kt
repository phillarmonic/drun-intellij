package com.phillarmonic.drun.editor

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class DrunFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val file = root.containingFile ?: return FoldingDescriptor.EMPTY_ARRAY
        val result = mutableListOf<FoldingDescriptor>()
        addIndentedBlocks(file, document, result)
        addBlockComments(file, result)
        return result.toTypedArray()
    }

    private fun addIndentedBlocks(file: PsiFile, document: Document, result: MutableList<FoldingDescriptor>) {
        val stack = ArrayDeque<BlockData>()
        for (line in 0 until document.lineCount) {
            val start = document.getLineStartOffset(line)
            val end = document.getLineEndOffset(line)
            val text = document.charsSequence.subSequence(start, end).toString()
            if (text.isBlank()) continue
            val indent = text.takeWhile { it == ' ' || it == '\t' }.fold(0) { count, c -> count + if (c == '\t') 2 else 1 }
            while (stack.isNotEmpty() && indent <= stack.last().indent) closeBlock(file, document, stack.removeLast(), line - 1, result)
            if (text.trimEnd().endsWith(':')) stack.addLast(BlockData(indent, line))
        }
        while (stack.isNotEmpty()) closeBlock(file, document, stack.removeLast(), document.lineCount - 1, result)
    }

    private fun closeBlock(file: PsiFile, document: Document, block: BlockData, endLine: Int, result: MutableList<FoldingDescriptor>) {
        if (endLine <= block.startLine) return
        val start = document.getLineEndOffset(block.startLine)
        val end = document.getLineEndOffset(endLine)
        if (end > start) result += FoldingDescriptor(file.node, TextRange(start, end))
    }

    private data class BlockData(val indent: Int, val startLine: Int)

    private fun addBlockComments(file: PsiFile, result: MutableList<FoldingDescriptor>) {
        val regex = Regex("/\\*[\\s\\S]*?\\*/")
        regex.findAll(file.text).filter { '\n' in it.value }.forEach {
            result += FoldingDescriptor(file.node, TextRange(it.range.first, it.range.last + 1))
        }
    }

    override fun getPlaceholderText(node: ASTNode) = "…"
    override fun isCollapsedByDefault(node: ASTNode) = false
}
