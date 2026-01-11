package com.amakaflow.companion.simulation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.simulationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "simulation_settings"
)

/**
 * Persisted settings for workout simulation mode.
 * Uses DataStore for reactive, type-safe preferences.
 */
@Singleton
class SimulationSettings @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.simulationDataStore

    companion object {
        private val ENABLED = booleanPreferencesKey("simulation_enabled")
        private val SPEED = doublePreferencesKey("simulation_speed")
        private val PROFILE = stringPreferencesKey("simulation_profile")
        private val GENERATE_HEALTH = booleanPreferencesKey("generate_health_data")
        private val RESTING_HR = intPreferencesKey("resting_hr")
        private val MAX_HR = intPreferencesKey("max_hr")
        // AMA-308: Weight simulation settings
        private val SIMULATE_WEIGHT = booleanPreferencesKey("simulate_weight")
        private val WEIGHT_PROFILE = stringPreferencesKey("weight_profile")

        // Speed presets
        val SPEED_OPTIONS = listOf(1.0, 10.0, 30.0, 60.0)

        // Profile options
        val PROFILE_OPTIONS = listOf("efficient", "casual", "distracted")

        // AMA-308: Weight profile options
        val WEIGHT_PROFILE_OPTIONS = listOf("beginner", "intermediate", "advanced")
    }

    /**
     * Whether simulation mode is enabled.
     */
    val isEnabled: Flow<Boolean> = dataStore.data.map { it[ENABLED] ?: false }

    /**
     * Speed multiplier (1x, 10x, 30x, 60x).
     */
    val speed: Flow<Double> = dataStore.data.map { it[SPEED] ?: 10.0 }

    /**
     * User behavior profile name.
     */
    val profileName: Flow<String> = dataStore.data.map { it[PROFILE] ?: "casual" }

    /**
     * Whether to generate fake health data (HR, calories, steps).
     */
    val generateHealthData: Flow<Boolean> = dataStore.data.map { it[GENERATE_HEALTH] ?: true }

    /**
     * Custom resting heart rate for HR simulation.
     */
    val restingHR: Flow<Int> = dataStore.data.map { it[RESTING_HR] ?: 70 }

    /**
     * Custom max heart rate for HR simulation.
     */
    val maxHR: Flow<Int> = dataStore.data.map { it[MAX_HR] ?: 175 }

    /**
     * AMA-308: Whether to automatically select weights for strength exercises.
     */
    val simulateWeight: Flow<Boolean> = dataStore.data.map { it[SIMULATE_WEIGHT] ?: true }

    /**
     * AMA-308: Weight profile for simulated weights (beginner, intermediate, advanced).
     */
    val weightProfile: Flow<String> = dataStore.data.map { it[WEIGHT_PROFILE] ?: "intermediate" }

    /**
     * Enable or disable simulation mode.
     */
    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[ENABLED] = enabled }
    }

    /**
     * Set the speed multiplier.
     */
    suspend fun setSpeed(speed: Double) {
        dataStore.edit { it[SPEED] = speed }
    }

    /**
     * Set the user behavior profile.
     */
    suspend fun setProfile(profile: String) {
        dataStore.edit { it[PROFILE] = profile }
    }

    /**
     * Enable or disable fake health data generation.
     */
    suspend fun setGenerateHealthData(generate: Boolean) {
        dataStore.edit { it[GENERATE_HEALTH] = generate }
    }

    /**
     * Set custom resting heart rate.
     */
    suspend fun setRestingHR(hr: Int) {
        dataStore.edit { it[RESTING_HR] = hr }
    }

    /**
     * Set custom max heart rate.
     */
    suspend fun setMaxHR(hr: Int) {
        dataStore.edit { it[MAX_HR] = hr }
    }

    /**
     * AMA-308: Enable or disable automatic weight selection.
     */
    suspend fun setSimulateWeight(enabled: Boolean) {
        dataStore.edit { it[SIMULATE_WEIGHT] = enabled }
    }

    /**
     * AMA-308: Set the weight profile (beginner, intermediate, advanced).
     */
    suspend fun setWeightProfile(profile: String) {
        dataStore.edit { it[WEIGHT_PROFILE] = profile }
    }

    /**
     * Get the UserBehaviorProfile for the current profile name.
     */
    fun getBehaviorProfile(name: String): UserBehaviorProfile = UserBehaviorProfile.fromName(name)

    /**
     * Get the HRProfile based on current settings.
     */
    fun getHRProfile(restingHR: Int, maxHR: Int): HRProfile = HRProfile.custom(restingHR, maxHR)

    /**
     * Synchronously check if simulation is enabled.
     * Use sparingly - prefer the Flow-based isEnabled.
     */
    fun isEnabledSync(): Boolean = runBlocking { isEnabled.first() }

    /**
     * Get all current settings as a snapshot.
     */
    suspend fun getSnapshot(): SimulationSnapshot {
        val prefs = dataStore.data.first()
        return SimulationSnapshot(
            isEnabled = prefs[ENABLED] ?: false,
            speed = prefs[SPEED] ?: 10.0,
            profileName = prefs[PROFILE] ?: "casual",
            generateHealthData = prefs[GENERATE_HEALTH] ?: true,
            restingHR = prefs[RESTING_HR] ?: 70,
            maxHR = prefs[MAX_HR] ?: 175,
            simulateWeight = prefs[SIMULATE_WEIGHT] ?: true,
            weightProfile = prefs[WEIGHT_PROFILE] ?: "intermediate"
        )
    }
}

/**
 * Snapshot of all simulation settings at a point in time.
 */
data class SimulationSnapshot(
    val isEnabled: Boolean,
    val speed: Double,
    val profileName: String,
    val generateHealthData: Boolean,
    val restingHR: Int,
    val maxHR: Int,
    // AMA-308: Weight simulation settings
    val simulateWeight: Boolean,
    val weightProfile: String
) {
    /**
     * Get the behavior profile for this snapshot.
     */
    val behaviorProfile: UserBehaviorProfile
        get() = UserBehaviorProfile.fromName(profileName)

    /**
     * Get the HR profile for this snapshot.
     */
    val hrProfile: HRProfile
        get() = HRProfile.custom(restingHR, maxHR)

    /**
     * AMA-308: Get the weight profile enum for this snapshot.
     */
    val weightProfileEnum: WeightProfile
        get() = WeightProfile.fromName(weightProfile)
}
