package com.phillarmonic.drun.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspClientManager

object DrunLspRestarter {
    @JvmStatic
    fun restart(project: Project) {
        LspClientManager.getInstance(project).stopAndRestartClientsIfNeeded(DrunLspServerSupportProvider::class.java)
    }
}
