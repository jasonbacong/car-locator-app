package com.jasongrech.carlocator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "car_locator_prefs")

data class HomeLocation(val lat: Double, val lng: Double)

class PrefsRepository(private val context: Context) {

    private object Keys {
        // Superseded by the CarDevice table (multiple cars) — kept only so
        // CarLocatorApp can migrate a single previously-selected car into it once.
        val CAR_DEVICE_ADDRESS = stringPreferencesKey("car_device_address")
        val CAR_DEVICE_NAME = stringPreferencesKey("car_device_name")
        val HOME_LAT = doublePreferencesKey("home_lat")
        val HOME_LNG = doublePreferencesKey("home_lng")
        val HOME_RADIUS_M = floatPreferencesKey("home_radius_m")
        val FEATURE_ENABLED = booleanPreferencesKey("feature_enabled")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val legacyCarDeviceAddress: Flow<String?> = context.dataStore.data.map { it[Keys.CAR_DEVICE_ADDRESS] }
    val legacyCarDeviceName: Flow<String?> = context.dataStore.data.map { it[Keys.CAR_DEVICE_NAME] }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }

    val homeLocation: Flow<HomeLocation?> = context.dataStore.data.map { prefs ->
        val lat = prefs[Keys.HOME_LAT]
        val lng = prefs[Keys.HOME_LNG]
        if (lat != null && lng != null) HomeLocation(lat, lng) else null
    }

    val homeRadiusMeters: Flow<Float> = context.dataStore.data.map { it[Keys.HOME_RADIUS_M] ?: 150f }
    val featureEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.FEATURE_ENABLED] ?: true }

    suspend fun clearLegacyCarDevice() {
        context.dataStore.edit {
            it.remove(Keys.CAR_DEVICE_ADDRESS)
            it.remove(Keys.CAR_DEVICE_NAME)
        }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = seen }
    }

    suspend fun setHomeLocation(lat: Double, lng: Double) {
        context.dataStore.edit {
            it[Keys.HOME_LAT] = lat
            it[Keys.HOME_LNG] = lng
        }
    }

    suspend fun setHomeRadiusMeters(radius: Float) {
        context.dataStore.edit { it[Keys.HOME_RADIUS_M] = radius }
    }

    suspend fun setFeatureEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FEATURE_ENABLED] = enabled }
    }
}
