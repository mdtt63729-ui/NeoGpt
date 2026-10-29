package com.neogpt.client.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.neogpt.client.domain.model.AppPreferences
import com.neogpt.client.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "neogpt_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val baseUrl = stringPreferencesKey("provider_base_url")
        val apiKey = stringPreferencesKey("provider_api_key")
        val model = stringPreferencesKey("selected_model")
        val sendOnEnter = booleanPreferencesKey("send_on_enter")
        val haptics = booleanPreferencesKey("haptics")
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val displayName = stringPreferencesKey("display_name")
        val themeLightMigration = booleanPreferencesKey("theme_light_migration_v2")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            themeMode = runCatching {
                ThemeMode.valueOf(prefs[Keys.theme] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            baseUrl = prefs[Keys.baseUrl].orEmpty(),
            apiKey = prefs[Keys.apiKey].orEmpty(),
            modelId = prefs[Keys.model] ?: "gpt-5",
            sendOnEnter = prefs[Keys.sendOnEnter] ?: true,
            haptics = prefs[Keys.haptics] ?: true,
            onboardingDone = prefs[Keys.onboardingDone] ?: false,
            displayName = prefs[Keys.displayName].orEmpty(),
        )
    }

    /**
     * One-time migration: this build defaults to the white theme, so an install
     * that still carries the old "follow system" value is switched once.
     */
    suspend fun migrateToDefaultLightTheme() {
        context.dataStore.edit { prefs ->
            if (prefs[Keys.themeLightMigration] != true) {
                prefs[Keys.theme] = ThemeMode.LIGHT.name
                prefs[Keys.themeLightMigration] = true
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setProvider(baseUrl: String, apiKey: String, modelId: String) {
        context.dataStore.edit {
            it[Keys.baseUrl] = baseUrl.trim()
            it[Keys.apiKey] = apiKey.trim()
            it[Keys.model] = modelId.trim()
        }
    }

    suspend fun setModel(modelId: String) {
        context.dataStore.edit { it[Keys.model] = modelId }
    }

    suspend fun setSendOnEnter(enabled: Boolean) {
        context.dataStore.edit { it[Keys.sendOnEnter] = enabled }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.dataStore.edit { it[Keys.haptics] = enabled }
    }

    /**
     * Stores a local-only profile name (never sent anywhere) and marks the
     * welcome screen as completed so the app opens straight into chat.
     */
    suspend fun completeOnboarding(name: String) {
        context.dataStore.edit {
            it[Keys.displayName] = name.trim()
            it[Keys.onboardingDone] = true
        }
    }

    suspend fun skipOnboarding() {
        context.dataStore.edit { it[Keys.onboardingDone] = true }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { it[Keys.onboardingDone] = false }
    }
}
