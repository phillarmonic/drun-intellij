package com.phillarmonic.drun.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.phillarmonic.drun.lexer.DrunTokenTypes

class DrunBraceMatcher : PairedBraceMatcher {
    override fun getPairs() = arrayOf(
        BracePair(DrunTokenTypes.LBRACE, DrunTokenTypes.RBRACE, true),
        BracePair(DrunTokenTypes.LBRACKET, DrunTokenTypes.RBRACKET, false),
        BracePair(DrunTokenTypes.LPAREN, DrunTokenTypes.RPAREN, false),
    )
    override fun isPairedBracesAllowedBeforeType(leftBraceType: IElementType, contextType: IElementType?) = true
    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int) = openingBraceOffset
}
