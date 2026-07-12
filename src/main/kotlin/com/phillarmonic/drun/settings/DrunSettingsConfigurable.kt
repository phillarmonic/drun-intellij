package com.phillarmonic.drun.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

class DrunGlobalConfigurable : SearchableConfigurable {
    private val enabled = JBCheckBox("Enable the Drun language server")
    private val path = JBTextField()
    override fun getId() = "com.phillarmonic.drun.settings.global"
    override fun getDisplayName() = "Drun"
    override fun createComponent(): JComponent = JPanel(BorderLayout()).apply {
        add(FormBuilder.createFormBuilder().addComponent(enabled).addLabeledComponent(JBLabel("Path or command name for xdrun:"), path).addComponentFillVertically(JPanel(), 0).panel)
        reset()
    }
    override fun isModified(): Boolean = DrunGlobalSettings.getInstance().state.let { it.enableLanguageServer != enabled.isSelected || it.xdrunPath != path.text }
    override fun apply() {
        DrunGlobalSettings.getInstance().state.apply { enableLanguageServer = enabled.isSelected; xdrunPath = path.text.trim().ifEmpty { "xdrun" } }
        ProjectManager.getInstance().openProjects.forEach { restartServer(it) }
    }
    override fun reset() = DrunGlobalSettings.getInstance().state.let { enabled.isSelected = it.enableLanguageServer; path.text = it.xdrunPath }
}

class DrunProjectConfigurable(private val project: Project) : SearchableConfigurable {
    private val mode = JComboBox(LspEnablement.entries.toTypedArray())
    private val path = JBTextField()
    override fun getId() = "com.phillarmonic.drun.settings.project"
    override fun getDisplayName() = "Drun"
    override fun createComponent(): JComponent = JPanel(BorderLayout()).apply {
        mode.renderer = LspEnablementRenderer()
        add(FormBuilder.createFormBuilder().addLabeledComponent(JBLabel("Language server:"), mode).addLabeledComponent(JBLabel("Project xdrun override (blank inherits global):"), path).addComponentFillVertically(JPanel(), 0).panel)
        reset()
    }
    override fun isModified(): Boolean = DrunProjectSettings.getInstance(project).state.let { it.enableLanguageServer != mode.selectedItem || it.xdrunPathOverride != path.text }
    override fun apply() {
        DrunProjectSettings.getInstance(project).state.apply { enableLanguageServer = mode.selectedItem as LspEnablement; xdrunPathOverride = path.text.trim() }
        restartServer(project)
    }
    override fun reset() = DrunProjectSettings.getInstance(project).state.let { mode.selectedItem = it.enableLanguageServer; path.text = it.xdrunPathOverride }
}

private fun restartServer(project: Project) {
    // The implementation class links against JetBrains' optional LSP module. Load it only
    // when that module is present so syntax support remains usable in Android Studio.
    runCatching {
        val restarter = Class.forName("com.phillarmonic.drun.lsp.DrunLspRestarter")
        restarter.getMethod("restart", Project::class.java).invoke(null, project)
    }
}

private class LspEnablementRenderer : javax.swing.DefaultListCellRenderer() {
    override fun getListCellRendererComponent(list: javax.swing.JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): java.awt.Component {
        val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        text = when (value as? LspEnablement) {
            LspEnablement.INHERIT -> "Inherit global setting"
            LspEnablement.ENABLED -> "Enabled"
            LspEnablement.DISABLED -> "Disabled"
            null -> ""
        }
        return component
    }
}
