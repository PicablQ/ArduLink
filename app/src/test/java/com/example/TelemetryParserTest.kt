package com.example

import com.example.data.TelemetryParser
import com.example.model.SensorChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TelemetryParserTest {

    @Test
    fun `parses key value telemetry line`() {
        val parsed = TelemetryParser.parseLine("TEMP:24.8,HUM:56.2,DIST:42,VOLT:11.8,LIGHT:75")
        assertEquals(24.8f, parsed[SensorChannel.TEMPERATURE] ?: 0f, 0.01f)
        assertEquals(56.2f, parsed[SensorChannel.HUMIDITY] ?: 0f, 0.01f)
        assertEquals(42.0f, parsed[SensorChannel.DISTANCE] ?: 0f, 0.01f)
        assertEquals(11.8f, parsed[SensorChannel.VOLTAGE] ?: 0f, 0.01f)
        assertEquals(75.0f, parsed[SensorChannel.LIGHT] ?: 0f, 0.01f)
    }

    @Test
    fun `parses json telemetry line`() {
        val parsed = TelemetryParser.parseLine("""{"temp":26.1,"hum":61,"dist":18.5}""")
        assertEquals(26.1f, parsed[SensorChannel.TEMPERATURE] ?: 0f, 0.01f)
        assertEquals(61.0f, parsed[SensorChannel.HUMIDITY] ?: 0f, 0.01f)
        assertEquals(18.5f, parsed[SensorChannel.DISTANCE] ?: 0f, 0.01f)
    }

    @Test
    fun `parses csv telemetry line`() {
        val parsed = TelemetryParser.parseLine("23.4,50,95.5,12.1")
        assertEquals(23.4f, parsed[SensorChannel.TEMPERATURE] ?: 0f, 0.01f)
        assertEquals(50.0f, parsed[SensorChannel.HUMIDITY] ?: 0f, 0.01f)
        assertEquals(95.5f, parsed[SensorChannel.DISTANCE] ?: 0f, 0.01f)
        assertEquals(12.1f, parsed[SensorChannel.VOLTAGE] ?: 0f, 0.01f)
    }

    @Test
    fun `ignores invalid non telemetry ack strings`() {
        val parsed = TelemetryParser.parseLine("ACK:R1_ON")
        assertTrue(parsed.isEmpty())
    }
}
