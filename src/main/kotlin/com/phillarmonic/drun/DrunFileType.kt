package com.phillarmonic.drun

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object DrunFileType : LanguageFileType(DrunLanguage) {
    override fun getName() = "Drun"
    override fun getDescription() = "Drun automation specification"
    override fun getDefaultExtension() = "drun"
    override fun getIcon(): Icon = DrunIcons.FILE
}
