package com.phillarmonic.drun.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager

object DrunLspRestarter {
    @JvmStatic
    fun restart(project: Project) {
        LspServerManager.getInstance(project).stopAndRestartIfNeeded(DrunLspServerSupportProvider::class.java)
    }
}
