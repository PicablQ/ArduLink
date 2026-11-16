package com.example.data

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.example.model.AppLanguage
import java.util.Locale

fun resolveLocale(language: AppLanguage): Locale = when (language) {
    AppLanguage.SYSTEM -> Resources.getSystem().configuration.locales[0] ?: Locale.getDefault()
    AppLanguage.TURKISH -> Locale.forLanguageTag("tr-TR")
    AppLanguage.ENGLISH -> Locale.forLanguageTag("en-US")
}

fun isTurkishLocale(language: AppLanguage): Boolean {
    return resolveLocale(language).language.equals("tr", ignoreCase = true)
}

fun Context.withAppLanguage(language: AppLanguage): Context {
    val locale = resolveLocale(language)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
