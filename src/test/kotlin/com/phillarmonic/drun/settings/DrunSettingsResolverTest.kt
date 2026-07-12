package com.phillarmonic.drun.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class DrunSettingsResolverTest {
    @Test fun `inherits global defaults`() {
        assertEquals(EffectiveLspSettings(true, "xdrun"), DrunSettingsResolver.resolve(GlobalState(), ProjectState()))
    }

    @Test fun `project enablement overrides global`() {
        assertEquals(true, DrunSettingsResolver.resolve(GlobalState(false), ProjectState(LspEnablement.ENABLED)).enabled)
        assertEquals(false, DrunSettingsResolver.resolve(GlobalState(true), ProjectState(LspEnablement.DISABLED)).enabled)
    }

    @Test fun `project path takes precedence and blank path inherits`() {
        assertEquals("/opt/xdrun", DrunSettingsResolver.resolve(GlobalState(xdrunPath = "/usr/bin/xdrun"), ProjectState(xdrunPathOverride = "/opt/xdrun")).executable)
        assertEquals("/usr/bin/xdrun", DrunSettingsResolver.resolve(GlobalState(xdrunPath = "/usr/bin/xdrun"), ProjectState(xdrunPathOverride = " ")).executable)
    }
}
