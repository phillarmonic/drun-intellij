package com.phillarmonic.drun.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

enum class LspEnablement { INHERIT, ENABLED, DISABLED }

data class GlobalState(var enableLanguageServer: Boolean = true, var xdrunPath: String = "xdrun")
data class ProjectState(var enableLanguageServer: LspEnablement = LspEnablement.INHERIT, var xdrunPathOverride: String = "")
data class EffectiveLspSettings(val enabled: Boolean, val executable: String)

@Service(Service.Level.APP)
@State(name = "DrunGlobalSettings", storages = [Storage("drun.xml")])
class DrunGlobalSettings : PersistentStateComponent<GlobalState> {
    private var state = GlobalState()
    override fun getState() = state
    override fun loadState(state: GlobalState) { this.state = state }
    companion object { fun getInstance(): DrunGlobalSettings = ApplicationManager.getApplication().getService(DrunGlobalSettings::class.java) }
}

@Service(Service.Level.PROJECT)
@State(name = "DrunProjectSettings", storages = [Storage("drun.xml")])
class DrunProjectSettings : PersistentStateComponent<ProjectState> {
    private var state = ProjectState()
    override fun getState() = state
    override fun loadState(state: ProjectState) { this.state = state }
    companion object { fun getInstance(project: Project): DrunProjectSettings = project.getService(DrunProjectSettings::class.java) }
}

object DrunSettingsResolver {
    fun resolve(project: Project): EffectiveLspSettings {
        val global = DrunGlobalSettings.getInstance().state
        val local = DrunProjectSettings.getInstance(project).state
        return resolve(global, local)
    }

    internal fun resolve(global: GlobalState, local: ProjectState): EffectiveLspSettings {
        val enabled = when (local.enableLanguageServer) {
            LspEnablement.INHERIT -> global.enableLanguageServer
            LspEnablement.ENABLED -> true
            LspEnablement.DISABLED -> false
        }
        return EffectiveLspSettings(enabled, local.xdrunPathOverride.trim().ifEmpty { global.xdrunPath.trim().ifEmpty { "xdrun" } })
    }
}
