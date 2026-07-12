package com.phillarmonic.drun.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.phillarmonic.drun.settings.DrunSettingsResolver
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile

class DrunLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
        if (file.extension != "drun") return
        val settings = DrunSettingsResolver.resolve(project)
        if (!settings.enabled) return
        if (!isExecutableAvailable(settings.executable)) {
            LOG.warn("Drun language server executable is unavailable: ${settings.executable}")
            NotificationGroupManager.getInstance().getNotificationGroup("Drun Language Server")
                .createNotification("Drun language server unavailable", "Could not execute '${settings.executable}'. Syntax highlighting remains available.", NotificationType.WARNING)
                .addAction(com.intellij.notification.NotificationAction.createSimple("Open Drun settings") {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "Drun")
                }).notify(project)
            return
        }
        serverStarter.ensureServerStarted(DrunLspServerDescriptor(project, settings.executable))
    }

    companion object {
        private val LOG = Logger.getInstance(DrunLspServerSupportProvider::class.java)

        internal fun isExecutableAvailable(command: String): Boolean {
            val looksLikePath = command.contains('/') || command.contains('\\') || Path.of(command).isAbsolute
            return if (looksLikePath) runCatching { Files.isRegularFile(Path.of(command)) && Files.isExecutable(Path.of(command)) }.getOrDefault(false)
            else executableOnPath(command)
        }

        private fun executableOnPath(command: String): Boolean =
            (System.getenv("PATH") ?: "").split(java.io.File.pathSeparatorChar).any { directory ->
                runCatching { Path.of(directory, command).let { it.isRegularFile() && it.isExecutable() } }.getOrDefault(false)
            }
    }
}

internal class DrunLspServerDescriptor(project: Project, private val executable: String) :
    ProjectWideLspServerDescriptor(project, "Drun") {
    override fun isSupportedFile(file: VirtualFile) = file.extension == "drun"
    override fun createCommandLine() = GeneralCommandLine(executable, "cmd:lsp").withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
}
