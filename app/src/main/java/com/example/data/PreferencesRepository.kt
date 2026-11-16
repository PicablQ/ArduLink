package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.CarControlMode
import com.example.model.CircuitDevice
import com.example.model.DeviceControlType
import com.example.model.DeviceIconType
import com.example.model.LineEnding
import com.example.model.MultiSwitchItem
import com.example.model.VoiceCommandRule
import com.example.model.defaultCircuitDevices
import com.example.model.defaultMultiSwitches
import com.example.model.defaultVoiceRules
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "ardulink_prefs")

data class UserPreferences(
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val lineEnding: LineEnding = LineEnding.LF,
    val carControlMode: CarControlMode = CarControlMode.TWO_HAND,
    val circuitDevices: List<CircuitDevice> = defaultCircuitDevices(),
    val multiSwitches: List<MultiSwitchItem> = defaultMultiSwitches(),
    val voiceRules: List<VoiceCommandRule> = defaultVoiceRules(),
    val sendUnmatchedVoice: Boolean = false
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LINE_ENDING = stringPreferencesKey("line_ending")
        val CAR_CONTROL_MODE = stringPreferencesKey("car_control_mode")
        val CIRCUIT_DEVICES_JSON = stringPreferencesKey("circuit_devices_json")
        val MULTI_SWITCHES_JSON = stringPreferencesKey("multi_switches_json")
        val VOICE_RULES_JSON = stringPreferencesKey("voice_rules_json")
        val SEND_UNMATCHED_VOICE = booleanPreferencesKey("send_unmatched_voice")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val language = prefs[Keys.APP_LANGUAGE]
            ?.let { name -> AppLanguage.entries.find { it.name == name } }
            ?: AppLanguage.SYSTEM

        val isTr = isTurkishLocale(language)

        val theme = prefs[Keys.THEME_MODE]
            ?.let { name -> ThemeMode.entries.find { it.name == name } }
            ?: ThemeMode.DARK

        val lineEnding = prefs[Keys.LINE_ENDING]
            ?.let { name -> LineEnding.entries.find { it.name == name } }
            ?: LineEnding.LF

        val carMode = prefs[Keys.CAR_CONTROL_MODE]
            ?.let { name -> CarControlMode.entries.find { it.name == name } }
            ?: CarControlMode.TWO_HAND

        val devices = prefs[Keys.CIRCUIT_DEVICES_JSON]
            ?.let { decodeDevices(it) }
            ?: defaultCircuitDevices(isTr)

        val multiSwitches = prefs[Keys.MULTI_SWITCHES_JSON]
            ?.let { decodeMultiSwitches(it) }
            ?: defaultMultiSwitches(isTr)

        val voiceRules = prefs[Keys.VOICE_RULES_JSON]
            ?.let { decodeVoiceRules(it) }
            ?: defaultVoiceRules(isTr)

        val sendUnmatched = prefs[Keys.SEND_UNMATCHED_VOICE] ?: false

        UserPreferences(
            appLanguage = language,
            themeMode = theme,
            lineEnding = lineEnding,
            carControlMode = carMode,
            circuitDevices = devices,
            multiSwitches = multiSwitches,
            voiceRules = voiceRules,
            sendUnmatchedVoice = sendUnmatched
        )
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.APP_LANGUAGE] = language.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLineEnding(lineEnding: LineEnding) {
        context.dataStore.edit { it[Keys.LINE_ENDING] = lineEnding.name }
    }

    suspend fun setCarControlMode(mode: CarControlMode) {
        context.dataStore.edit { it[Keys.CAR_CONTROL_MODE] = mode.name }
    }

    suspend fun saveCircuitDevices(devices: List<CircuitDevice>) {
        context.dataStore.edit { it[Keys.CIRCUIT_DEVICES_JSON] = encodeDevices(devices) }
    }

    suspend fun saveMultiSwitches(items: List<MultiSwitchItem>) {
        context.dataStore.edit { it[Keys.MULTI_SWITCHES_JSON] = encodeMultiSwitches(items) }
    }

    suspend fun saveVoiceRules(rules: List<VoiceCommandRule>) {
        context.dataStore.edit { it[Keys.VOICE_RULES_JSON] = encodeVoiceRules(rules) }
    }

    suspend fun setSendUnmatchedVoice(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SEND_UNMATCHED_VOICE] = enabled }
    }

    private fun encodeDevices(devices: List<CircuitDevice>): String {
        val arr = JSONArray()
        devices.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("name", d.name)
                put("subtitle", d.subtitle)
                put("pinLabel", d.pinLabel)
                put("type", d.type.name)
                put("iconType", d.iconType.name)
                put("commandOn", d.commandOn)
                put("commandOff", d.commandOff)
                put("sliderPrefix", d.sliderPrefix)
                put("sliderMin", d.sliderMin)
                put("sliderMax", d.sliderMax)
                put("sliderValue", d.sliderValue)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun decodeDevices(raw: String): List<CircuitDevice>? {
        return try {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return emptyList()
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        CircuitDevice(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            subtitle = obj.optString("subtitle", ""),
                            pinLabel = obj.optString("pinLabel", "GPIO"),
                            type = DeviceControlType.entries.find {
                                it.name == obj.optString("type")
                            } ?: DeviceControlType.TOGGLE,
                            iconType = DeviceIconType.entries.find {
                                it.name == obj.optString("iconType")
                            } ?: DeviceIconType.CUSTOM,
                            commandOn = obj.getString("commandOn"),
                            commandOff = obj.optString("commandOff", ""),
                            sliderPrefix = obj.optString("sliderPrefix", ""),
                            sliderMin = obj.optInt("sliderMin", 0),
                            sliderMax = obj.optInt("sliderMax", 180),
                            isOn = false,
                            sliderValue = obj.optInt("sliderValue", 90)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun encodeMultiSwitches(items: List<MultiSwitchItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("index", item.index)
                    put("name", item.name)
                    put("commandOn", item.commandOn)
                    put("commandOff", item.commandOff)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeMultiSwitches(raw: String): List<MultiSwitchItem>? {
        return try {
            val arr = JSONArray(raw)
            if (arr.length() != 10) return null
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        MultiSwitchItem(
                            index = obj.getInt("index"),
                            name = obj.getString("name"),
                            commandOn = obj.getString("commandOn"),
                            commandOff = obj.getString("commandOff"),
                            isOn = false
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun encodeVoiceRules(rules: List<VoiceCommandRule>): String {
        val arr = JSONArray()
        rules.forEach { rule ->
            arr.put(
                JSONObject().apply {
                    put("id", rule.id)
                    put("phrase", rule.phrase)
                    put("txCommand", rule.txCommand)
                }
            )
        }
        return arr.toString()
    }

    private fun decodeVoiceRules(raw: String): List<VoiceCommandRule>? {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        VoiceCommandRule(
                            id = obj.getString("id"),
                            phrase = obj.getString("phrase"),
                            txCommand = obj.getString("txCommand")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
