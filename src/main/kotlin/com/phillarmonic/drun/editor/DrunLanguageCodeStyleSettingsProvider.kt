package com.phillarmonic.drun.editor

import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.phillarmonic.drun.DrunLanguage

class DrunLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): Language = DrunLanguage
    override fun getIndentOptionsEditor() = SmartIndentOptionsEditor()
    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) consumer.showStandardOptions("SPACE_AFTER_COMMA")
    }
    override fun getCodeSample(settingsType: SettingsType) = "task \"build\":\n  if docker is available:\n    build docker image \"app\""
}
