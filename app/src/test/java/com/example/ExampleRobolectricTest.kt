package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.withAppLanguage
import com.example.model.AppLanguage
import com.example.model.defaultCircuitDevices
import com.example.model.defaultVoiceRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ArduLink", appName)
  }

  @Test
  fun `switches between english and turkish resources`() {
    val base = ApplicationProvider.getApplicationContext<Context>()
    val enContext = base.withAppLanguage(AppLanguage.ENGLISH)
    val trContext = base.withAppLanguage(AppLanguage.TURKISH)

    assertEquals("Car", enContext.getString(R.string.nav_car))
    assertEquals("Araba", trContext.getString(R.string.nav_car))

    assertEquals("Two-Hand", enContext.getString(R.string.car_mode_two_hand))
    assertEquals("Çift El", trContext.getString(R.string.car_mode_two_hand))

    assertEquals("FORWARD", enContext.getString(R.string.car_label_forward))
    assertEquals("İLERİ", trContext.getString(R.string.car_label_forward))

    assertTrue(defaultVoiceRules(isTurkish = false).any { it.phrase == "forward" })
    assertTrue(defaultVoiceRules(isTurkish = true).any { it.phrase == "ileri" })
    assertEquals("Relay Channel 1", defaultCircuitDevices(isTurkish = false).first().name)
    assertEquals("Röle Kanal 1", defaultCircuitDevices(isTurkish = true).first().name)
  }
}
