package com.phillarmonic.drun.lexer

import com.intellij.psi.tree.IElementType
import com.phillarmonic.drun.DrunLanguage

class DrunTokenType(debugName: String) : IElementType(debugName, DrunLanguage)

object DrunTokenTypes {
    @JvmField val LINE_COMMENT = DrunTokenType("LINE_COMMENT")
    @JvmField val BLOCK_COMMENT = DrunTokenType("BLOCK_COMMENT")
    @JvmField val KEYWORD = DrunTokenType("KEYWORD")
    @JvmField val ACTION = DrunTokenType("ACTION")
    @JvmField val SUB_STATEMENT = DrunTokenType("SUB_STATEMENT")
    @JvmField val TYPE = DrunTokenType("TYPE")
    @JvmField val MACRO = DrunTokenType("MACRO")
    @JvmField val CONSTANT = DrunTokenType("CONSTANT")
    @JvmField val NUMBER = DrunTokenType("NUMBER")
    @JvmField val STRING = DrunTokenType("STRING")
    @JvmField val STRING_ESCAPE = DrunTokenType("STRING_ESCAPE")
    @JvmField val INTERPOLATION = DrunTokenType("INTERPOLATION")
    @JvmField val VARIABLE = DrunTokenType("VARIABLE")
    @JvmField val ANNOTATION = DrunTokenType("ANNOTATION")
    @JvmField val DEFINITION = DrunTokenType("DEFINITION")
    @JvmField val PROPERTY = DrunTokenType("PROPERTY")
    @JvmField val OPERATOR = DrunTokenType("OPERATOR")
    @JvmField val LOGIC_OPERATOR = DrunTokenType("LOGIC_OPERATOR")
    @JvmField val WORD_COMPARISON = DrunTokenType("WORD_COMPARISON")
    @JvmField val LBRACE = DrunTokenType("LBRACE")
    @JvmField val RBRACE = DrunTokenType("RBRACE")
    @JvmField val LBRACKET = DrunTokenType("LBRACKET")
    @JvmField val RBRACKET = DrunTokenType("RBRACKET")
    @JvmField val LPAREN = DrunTokenType("LPAREN")
    @JvmField val RPAREN = DrunTokenType("RPAREN")
    @JvmField val COLON = DrunTokenType("COLON")
    @JvmField val COMMA = DrunTokenType("COMMA")
    @JvmField val IDENTIFIER = DrunTokenType("IDENTIFIER")
    @JvmField val BAD_CHARACTER = DrunTokenType("BAD_CHARACTER")
}
