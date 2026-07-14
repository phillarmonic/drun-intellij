package com.phillarmonic.drun.lsp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class DrunLspServerSupportProviderTest {
    @Test fun `finds executable on supplied path`() {
        val directory = Files.createTempDirectory("drun-lsp-path")
        val executable = Files.createFile(directory.resolve("xdrun")).toFile()
        executable.setExecutable(true)

        try {
            assertTrue(DrunLspServerSupportProvider.isExecutableAvailable("xdrun", directory.toString()))
            assertFalse(DrunLspServerSupportProvider.isExecutableAvailable("missing", directory.toString()))
        } finally {
            executable.delete()
            directory.toFile().delete()
        }
    }
}
