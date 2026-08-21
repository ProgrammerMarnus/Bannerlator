package com.winlator.star.container

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.HashMap

/**
 * JVM coverage for {@link ContainerConfig}: JSON round-trip, defaults resolution, gyro migration,
 * equality, and enum-shaped controller mapping. Pure JVM (org.json already on the unit-test
 * classpath). Same file-convention as the other app/src/test suites (junit4 + kotlin).
 */
class ContainerConfigTest {

    @Test
    fun jsonRoundTrip_equalsOriginal() {
        val config = ContainerConfig.builder(7)
            .name("My Container")
            .screenSize("1920x1080")
            .envVars("DXVK_HUD=fps")
            .graphicsDriver("wrapper")
            .renderer("Vulkan")
            .build()

        val reparsed = ContainerConfig.fromJson(config.toJsonString())
        assertEquals(config, reparsed)
        assertEquals(7, reparsed.id)
        assertEquals("My Container", reparsed.name)
        assertEquals("1920x1080", reparsed.screenSize)
        assertEquals("Vulkan", reparsed.renderer)
        assertEquals("wrapper", reparsed.graphicsDriver)
    }

    @Test
    fun emptyJson_resolvesToDefaults() {
        val config = ContainerConfig.fromJson("{}")
        assertEquals(ContainerConfig.DEFAULT_SCREEN_SIZE, config.screenSize)
        assertEquals(ContainerConfig.DEFAULT_GRAPHICS_DRIVER, config.graphicsDriver)
        assertEquals(ContainerConfig.DEFAULT_AUDIO_DRIVER, config.audioDriver)
        assertEquals(ContainerConfig.DEFAULT_DXWRAPPER, config.dxwrapper)
        assertEquals(ContainerConfig.DEFAULT_RENDERER, config.renderer)
        assertEquals(ContainerConfig.DEFAULT_WINCOMPONENTS, config.wincomponents)
        // Name fallback derives from the (default-0) id.
        assertEquals("Container-0", config.name)
        assertTrue(config.extras.length() == 0)
    }

    @Test
    fun defaultsMatch_absentKeysOnId() {
        val json = """{"id":3,"name":"Legacy"}"""
        val config = ContainerConfig.fromJson(json)
        assertEquals(3, config.id)
        assertEquals("Legacy", config.name)
        // Absent keys fall back to the same defaults the app already computes.
        assertEquals(ContainerConfig.DEFAULT_SCREEN_SIZE, config.screenSize)
    }

    @Test
    fun migration_mapsLegacyGyroPrefs() {
        val out = HashMap<String, Object>()
        val migrated = ContainerIO.migrateLegacyPrefs(mapOf(
            "gyro_enabled" to true,
            "gyro_target" to 1,
            "gyro_activator" to 2,
            "gyro_sensitivity" to 2.5,
            "gyro_deadzone" to 0.05,
            "gyro_smoothing" to 0.5,
            "gyro_invert_x" to true,
            "gyro_invert_y" to false,
        ), out)
        assertTrue(migrated)
        assertEquals("1", out["gyroEnabled"])
        assertEquals("1", out["gyroTarget"])
        assertEquals("2", out["gyroActivator"])
        assertEquals("2.5", out["gyroSensitivity"])
        assertEquals("0.05", out["gyroDeadzone"])
        assertEquals("0.5", out["gyroSmoothing"])
        assertEquals("1", out["gyroInvertX"])
        assertEquals("0", out["gyroInvertY"])
        assertEquals(true, out["gyro_migrated_to_container"])
    }

    @Test
    fun migration_noopWhenAlreadyMigrated() {
        val out = HashMap<String, Object>()
        val migrated = ContainerIO.migrateLegacyPrefs(mapOf(
            "gyro_migrated_to_container" to true,
            "gyro_enabled" to false,
        ), out)
        assertFalse(migrated)
        assertTrue(out.isEmpty())
    }

    @Test
    fun migration_noopWithoutLegacyKeys() {
        val out = HashMap<String, Object>()
        val migrated = ContainerIO.migrateLegacyPrefs(mapOf("foo" to "bar"), out)
        assertFalse(migrated)
        assertTrue(out.isEmpty())
    }

    @Test
    fun equality_sameConfig_equal_different_notEqual() {
        val a = ContainerConfig.builder(1).name("A").screenSize("1280x720").build()
        val b = ContainerConfig.builder(1).name("A").screenSize("1280x720").build()
        val c = ContainerConfig.builder(2).name("B").screenSize("1280x720").build()
        assertEquals(a, b)
        assertFalse(a.equals(c))
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun enumValues_areStable() {
        val values = ContainerConfig.XrControllerMapping.values()
        assertEquals(10, values.size)
        // Spot-check the three sentinels that gate controller mapping bit packing.
        assertEquals("BUTTON_GRIP", ContainerConfig.XrControllerMapping.BUTTON_GRIP.name)
        assertEquals("THUMBSTICK_LEFT", ContainerConfig.XrControllerMapping.THUMBSTICK_LEFT.name)
        assertEquals("THUMBSTICK_RIGHT", ContainerConfig.XrControllerMapping.THUMBSTICK_RIGHT.name)
    }
}