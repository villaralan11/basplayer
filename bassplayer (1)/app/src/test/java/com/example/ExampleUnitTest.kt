package com.example

import com.example.data.model.EqPreset
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `eqPreset serializes and parses gains correctly`() {
        val gains = listOf(3.0f, 2.5f, 0.0f, -1.0f, -4.5f, 1.0f, 2.0f, 0.5f, 6.0f, 8.5f)
        val serialized = EqPreset.serializeGains(gains)
        assertEquals("3.0,2.5,0.0,-1.0,-4.5,1.0,2.0,0.5,6.0,8.5", serialized)

        val preset = EqPreset(
            id = "test_preset",
            name = "Test EQ Profile",
            bandGains = serialized,
            subBassGain = 0.5f,
            clarityGain = 0.8f
        )

        val parsedGains = preset.getGainsList()
        assertEquals(10, parsedGains.size)
        assertEquals(3.0f, parsedGains[0])
        assertEquals(-4.5f, parsedGains[4])
        assertEquals(8.5f, parsedGains[9])
    }

    @Test
    fun `eqPreset handles corrupted string gracefully by returning flat profile`() {
        val preset = EqPreset(
            id = "corrupted",
            name = "Corrupted EQ String",
            bandGains = "3.0,abc,0.0", // invalid data
            subBassGain = 0.0f,
            clarityGain = 0.0f
        )

        val parsedGains = preset.getGainsList()
        assertEquals(10, parsedGains.size)
        // Shouted be filled with default flat gains
        assertTrue(parsedGains.all { it == 0.0f })
    }
}
