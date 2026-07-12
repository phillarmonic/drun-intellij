package com.phillarmonic.drun.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object DrunTextAttributes {
    @JvmField val KEYWORD = key("DRUN_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    @JvmField val ACTION = key("DRUN_ACTION", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    @JvmField val TYPE = key("DRUN_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME)
    @JvmField val CONSTANT = key("DRUN_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)
    @JvmField val NUMBER = key("DRUN_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    @JvmField val STRING = key("DRUN_STRING", DefaultLanguageHighlighterColors.STRING)
    @JvmField val ESCAPE = key("DRUN_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
    @JvmField val INTERPOLATION = key("DRUN_INTERPOLATION", DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR)
    @JvmField val VARIABLE = key("DRUN_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
    @JvmField val ANNOTATION = key("DRUN_ANNOTATION", DefaultLanguageHighlighterColors.METADATA)
    @JvmField val DEFINITION = key("DRUN_DEFINITION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    @JvmField val OPERATOR = key("DRUN_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    @JvmField val PUNCTUATION = key("DRUN_PUNCTUATION", DefaultLanguageHighlighterColors.BRACES)
    @JvmField val LINE_COMMENT = key("DRUN_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    @JvmField val BLOCK_COMMENT = key("DRUN_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    @JvmField val BAD_CHARACTER = key("DRUN_BAD_CHARACTER", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE)

    private fun key(name: String, fallback: TextAttributesKey) = TextAttributesKey.createTextAttributesKey(name, fallback)
}
