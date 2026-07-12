package com.phillarmonic.drun.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.phillarmonic.drun.lexer.DrunLexerAdapter
import com.phillarmonic.drun.lexer.DrunTokenTypes

class DrunSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = DrunLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = pack(when (tokenType) {
        DrunTokenTypes.KEYWORD -> DrunTextAttributes.KEYWORD
        DrunTokenTypes.ACTION -> DrunTextAttributes.ACTION
        DrunTokenTypes.TYPE -> DrunTextAttributes.TYPE
        DrunTokenTypes.CONSTANT -> DrunTextAttributes.CONSTANT
        DrunTokenTypes.NUMBER -> DrunTextAttributes.NUMBER
        DrunTokenTypes.STRING -> DrunTextAttributes.STRING
        DrunTokenTypes.STRING_ESCAPE -> DrunTextAttributes.ESCAPE
        DrunTokenTypes.INTERPOLATION -> DrunTextAttributes.INTERPOLATION
        DrunTokenTypes.VARIABLE -> DrunTextAttributes.VARIABLE
        DrunTokenTypes.ANNOTATION -> DrunTextAttributes.ANNOTATION
        DrunTokenTypes.DEFINITION -> DrunTextAttributes.DEFINITION
        DrunTokenTypes.PROPERTY -> DrunTextAttributes.PROPERTY
        DrunTokenTypes.OPERATOR -> DrunTextAttributes.OPERATOR
        DrunTokenTypes.LBRACE, DrunTokenTypes.RBRACE, DrunTokenTypes.LBRACKET, DrunTokenTypes.RBRACKET,
        DrunTokenTypes.LPAREN, DrunTokenTypes.RPAREN,
        DrunTokenTypes.COLON, DrunTokenTypes.COMMA -> DrunTextAttributes.PUNCTUATION
        DrunTokenTypes.LINE_COMMENT -> DrunTextAttributes.LINE_COMMENT
        DrunTokenTypes.BLOCK_COMMENT -> DrunTextAttributes.BLOCK_COMMENT
        DrunTokenTypes.BAD_CHARACTER -> DrunTextAttributes.BAD_CHARACTER
        else -> null
    })
}
