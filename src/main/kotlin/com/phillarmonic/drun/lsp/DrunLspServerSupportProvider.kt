package com.phillarmonic.drun.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import com.intellij.util.EnvironmentUtil
import com.phillarmonic.drun.settings.DrunSettingsResolver
import java.nio.file.Path
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile

class DrunLspServerSupportProvider : LspIntegrationProvider {
    override fun fileOpened(project: Project, file: VirtualFile, clientStarter: LspIntegrationProvider.LspClientStarter) {
        if (file.extension != "drun") return
        val settings = DrunSettingsResolver.resolve(project)
        if (!settings.enabled) return
        val resolved = resolveExecutable(settings.executable)
        if (resolved == null) {
            LOG.warn("Drun language server executable is unavailable: ${settings.executable}")
            NotificationGroupManager.getInstance().getNotificationGroup("Drun Language Server")
                .createNotification("Drun language server unavailable", "Could not execute '${settings.executable}'. Syntax highlighting remains available.", NotificationType.WARNING)
                .addAction(com.intellij.notification.NotificationAction.createSimple("Open Drun settings") {
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, "Drun")
                }).notify(project)
            return
        }
        clientStarter.ensureClientStarted(DrunLspClientDescriptor(project, resolved))
    }

    companion object {
        private val LOG = Logger.getInstance(DrunLspServerSupportProvider::class.java)

        private val isWindows: Boolean = System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)

        internal fun isExecutableAvailable(
            command: String,
            path: String = EnvironmentUtil.getValue("PATH").orEmpty(),
            windows: Boolean = isWindows,
        ): Boolean = resolveExecutable(command, path, windows) != null

        /**
         * Resolves [command] to a concrete, launchable executable path.
         *
         * A path-like command is probed directly (adding Windows extensions when
         * needed). A bare command name is looked up on PATH first and, if not
         * found, in the well-known install locations: the default installer
         * directory takes priority over the Go bin directory.
         */
        internal fun resolveExecutable(
            command: String,
            path: String = EnvironmentUtil.getValue("PATH").orEmpty(),
            windows: Boolean = isWindows,
            fallbackDirs: List<Path> = defaultInstallDirs(windows),
        ): String? {
            val looksLikePath = command.contains('/') || command.contains('\\') || runCatching { Path.of(command).isAbsolute }.getOrDefault(false)
            if (looksLikePath) {
                return candidateNames(command, windows).firstOrNull { isExecutableFile(Path.of(it)) }
            }
            val searchDirs = path.split(java.io.File.pathSeparatorChar)
                .filter { it.isNotBlank() }
                .map { Path.of(it) } + fallbackDirs
            for (directory in searchDirs) {
                for (name in candidateNames(command, windows)) {
                    val candidate = runCatching { directory.resolve(name) }.getOrNull() ?: continue
                    if (isExecutableFile(candidate)) return candidate.toString()
                }
            }
            return null
        }

        // Well-known xdrun install locations, highest priority first: the default
        // installer directory, then the Go bin directory (for `go install`).
        internal fun defaultInstallDirs(windows: Boolean): List<Path> {
            val dirs = LinkedHashSet<Path>()
            fun add(value: String?, vararg more: String) {
                if (value.isNullOrBlank()) return
                runCatching { Path.of(value, *more) }.getOrNull()?.let { dirs.add(it) }
            }
            if (windows) {
                add(env("LOCALAPPDATA"), "Programs", "xdrun")
            } else {
                val home = env("HOME")
                add(home, "bin")
                add(home, ".local", "bin")
            }
            // Go bin (go install): $GOBIN, else $GOPATH/bin, else ~/go/bin.
            val goBin = env("GOBIN")
            if (!goBin.isNullOrBlank()) add(goBin)
            else {
                val goPath = env("GOPATH")
                if (!goPath.isNullOrBlank()) goPath.split(java.io.File.pathSeparatorChar).forEach { add(it, "bin") }
                else add(env(if (windows) "USERPROFILE" else "HOME"), "go", "bin")
            }
            return dirs.toList()
        }

        private fun env(name: String): String? = EnvironmentUtil.getValue(name) ?: System.getenv(name)

        private fun isExecutableFile(candidate: Path): Boolean =
            runCatching { candidate.isRegularFile() && candidate.isExecutable() }.getOrDefault(false)

        // On Windows a bare command name (e.g. "xdrun") is resolved against the
        // PATHEXT extensions, so also probe "xdrun.exe", "xdrun.cmd", etc.
        private fun candidateNames(command: String, windows: Boolean): List<String> {
            if (!windows) return listOf(command)
            val hasExtension = command.substringAfterLast('\\').substringAfterLast('/').contains('.')
            if (hasExtension) return listOf(command)
            val extensions = EnvironmentUtil.getValue("PATHEXT")
                ?.split(java.io.File.pathSeparatorChar)
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.takeIf { it.isNotEmpty() }
                ?: listOf(".COM", ".EXE", ".BAT", ".CMD")
            // Windows filesystems are case-insensitive, but tests may simulate
            // Windows on a case-sensitive one, so probe both extension cases.
            return listOf(command) + extensions.flatMap { ext ->
                val suffix = if (ext.startsWith('.')) ext else ".$ext"
                listOf(command + suffix, command + suffix.lowercase())
            }.distinct()
        }
    }
}

internal class DrunLspClientDescriptor(project: Project, private val executable: String) :
    ProjectWideLspClientDescriptor(project, "Drun") {
    override fun isSupportedFile(file: VirtualFile) = file.extension == "drun"
    override fun createCommandLine() = GeneralCommandLine(executable, "cmd:lsp").withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
}
