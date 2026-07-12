package com.phillarmonic.drun.editor

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.psi.tree.TokenSet
import com.phillarmonic.drun.lexer.DrunTokenTypes

class DrunQuoteHandler : SimpleTokenSetQuoteHandler(TokenSet.create(DrunTokenTypes.STRING))
