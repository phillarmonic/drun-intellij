package com.phillarmonic.drun.lsp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test fun `resolves windows exe from bare command name`() {
        val directory = Files.createTempDirectory("drun-lsp-win")
        val executable = Files.createFile(directory.resolve("xdrun.exe")).toFile()
        executable.setExecutable(true)

        try {
            val resolved = DrunLspServerSupportProvider.resolveExecutable(
                "xdrun",
                path = directory.toString(),
                windows = true,
                fallbackDirs = emptyList(),
            )
            assertNotNull(resolved)
            assertTrue(Files.isSameFile(executable.toPath(), java.nio.file.Path.of(resolved!!)))
        } finally {
            executable.delete()
            directory.toFile().delete()
        }
    }

    @Test fun `falls back to well-known install directory when not on path`() {
        val fallback = Files.createTempDirectory("drun-lsp-fallback")
        val executable = Files.createFile(fallback.resolve("xdrun")).toFile()
        executable.setExecutable(true)

        try {
            val resolved = DrunLspServerSupportProvider.resolveExecutable(
                "xdrun",
                path = "",
                windows = false,
                fallbackDirs = listOf(fallback),
            )
            assertNotNull(resolved)
            assertTrue(Files.isSameFile(executable.toPath(), java.nio.file.Path.of(resolved!!)))
        } finally {
            executable.delete()
            fallback.toFile().delete()
        }
    }

    @Test fun `returns null when executable is nowhere to be found`() {
        assertNull(
            DrunLspServerSupportProvider.resolveExecutable(
                "xdrun",
                path = "",
                windows = false,
                fallbackDirs = emptyList(),
            )
        )
    }
}
