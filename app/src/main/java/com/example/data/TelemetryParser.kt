package com.example.data

import com.example.model.SensorChannel
import org.json.JSONObject

object TelemetryParser {

    fun parseLine(rawLine: String): Map<SensorChannel, Float> {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty()) return emptyMap()

        // 1. Try JSON format: {"temp": 24.5, "hum": 58, "dist": 40, "volt": 11.8, "light": 72}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            parseJson(trimmed)?.let { if (it.isNotEmpty()) return it }
        }

        // 2. Try Key:Value or Key=Value pairs: TEMP:24.5,HUM:58,DIST:40,VOLT:11.8,LIGHT:72
        val kvResult = parseKeyValuePairs(trimmed)
        if (kvResult.isNotEmpty()) return kvResult

        // 3. Fallback CSV: 24.5,58,40,11.8,72
        return parseCsv(trimmed)
    }

    private fun parseJson(jsonText: String): Map<SensorChannel, Float>? {
        return try {
            val obj = JSONObject(jsonText)
            val result = mutableMapOf<SensorChannel, Float>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val channel = mapKeyToChannel(key) ?: continue
                val value = obj.optDouble(key, Double.NaN)
                if (!value.isNaN()) {
                    result[channel] = value.toFloat()
                }
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    private fun parseKeyValuePairs(text: String): Map<SensorChannel, Float> {
        val result = mutableMapOf<SensorChannel, Float>()
        val tokens = text.split(',', ';', '|', ' ')
        for (token in tokens) {
            val cleaned = token.trim()
            if (cleaned.isEmpty()) continue
            val sepIndex = when {
                ':' in cleaned -> cleaned.indexOf(':')
                '=' in cleaned -> cleaned.indexOf('=')
                else -> -1
            }
            if (sepIndex <= 0 || sepIndex >= cleaned.lastIndex) continue
            val key = cleaned.substring(0, sepIndex).trim()
            val rawVal = cleaned.substring(sepIndex + 1).trim()
            val channel = mapKeyToChannel(key) ?: continue
            val numeric = rawVal.replace(Regex("[^0-9.\\-]"), "").toFloatOrNull() ?: continue
            result[channel] = numeric
        }
        return result
    }

    private fun parseCsv(text: String): Map<SensorChannel, Float> {
        val parts = text.split(',')
        if (parts.size < 2) return emptyMap()
        val parsedNumbers = parts.mapNotNull { it.trim().toFloatOrNull() }
        if (parsedNumbers.size != parts.size) return emptyMap()

        val channels = SensorChannel.entries
        val result = mutableMapOf<SensorChannel, Float>()
        parsedNumbers.forEachIndexed { index, value ->
            if (index < channels.size) {
                result[channels[index]] = value
            }
        }
        return result
    }

    private fun mapKeyToChannel(rawKey: String): SensorChannel? {
        return when (rawKey.uppercase()) {
            "T", "TEMP", "TEMPERATURE", "SICAKLIK", "SIC" -> SensorChannel.TEMPERATURE
            "H", "HUM", "HUMIDITY", "NEM" -> SensorChannel.HUMIDITY
            "D", "DIST", "DISTANCE", "MESAFE", "SONAR", "US" -> SensorChannel.DISTANCE
            "V", "VOLT", "VOLTAGE", "BAT", "BATT", "VOLTAJ" -> SensorChannel.VOLTAGE
            "L", "LIGHT", "LDR", "LUX", "ISIK" -> SensorChannel.LIGHT
            else -> null
        }
    }
}
