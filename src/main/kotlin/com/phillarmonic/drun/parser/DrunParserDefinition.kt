package com.phillarmonic.drun.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.phillarmonic.drun.DrunFileType
import com.phillarmonic.drun.DrunLanguage
import com.phillarmonic.drun.lexer.DrunLexerAdapter
import com.phillarmonic.drun.lexer.DrunTokenTypes

class DrunParserDefinition : com.intellij.lang.ParserDefinition {
    override fun createLexer(project: Project?): Lexer = DrunLexerAdapter()
    override fun createParser(project: Project?): PsiParser = PsiParser { root, builder -> parse(root, builder) }
    override fun getFileNodeType() = FILE
    override fun getCommentTokens() = COMMENTS
    override fun getStringLiteralElements() = STRINGS
    override fun createElement(node: ASTNode): PsiElement = node.psi
    override fun createFile(viewProvider: FileViewProvider): PsiFile = DrunFile(viewProvider)

    private fun parse(root: com.intellij.psi.tree.IElementType, builder: PsiBuilder): ASTNode {
        val marker = builder.mark()
        while (!builder.eof()) builder.advanceLexer()
        marker.done(root)
        return builder.treeBuilt
    }

    companion object {
        @JvmField val FILE = IFileElementType(DrunLanguage)
        private val COMMENTS = TokenSet.create(DrunTokenTypes.LINE_COMMENT, DrunTokenTypes.BLOCK_COMMENT)
        private val STRINGS = TokenSet.create(DrunTokenTypes.STRING, DrunTokenTypes.STRING_ESCAPE, DrunTokenTypes.INTERPOLATION)
    }
}

class DrunFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DrunLanguage) {
    override fun getFileType(): FileType = DrunFileType
    override fun toString() = "Drun File"
}
