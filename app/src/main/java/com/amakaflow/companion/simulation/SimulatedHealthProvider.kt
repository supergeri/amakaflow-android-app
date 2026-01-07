package com.amakaflow.companion.simulation

import kotlinx.datetime.Instant
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

/**
 * A single heart rate sample with timestamp.
 */
data class HRSample(
    val timestamp: Instant,
    val value: Int
)

/**
 * Aggregated health data from a simulated workout.
 */
data class SimulatedHealthData(
    val hrSamples: List<HRSample>,
    val avgHR: Int,
    val maxHR: Int,
    val minHR: Int,
    val calories: Int,
    val steps: Int
)

/**
 * Exercise intensity levels for HR simulation.
 */
enum class ExerciseIntensity {
    REST,
    LOW,
    MODERATE,
    HIGH
}

/**
 * Generates realistic simulated health data during workouts.
 * Creates heart rate curves that ramp up during work and recover during rest.
 *
 * @param profile The HR profile determining resting/max HR and recovery rate
 * @param clock The clock to use for timestamps
 */
class SimulatedHealthProvider(
    private val profile: HRProfile,
    private val clock: WorkoutClock
) {
    var currentHR: Int = profile.restingHR
        private set

    private val samples = mutableListOf<HRSample>()
    private var totalCalories: Double = 0.0
    private var totalSteps: Int = 0

    /**
     * Simulate heart rate during a work interval.
     * HR ramps up exponentially toward the target based on intensity.
     *
     * @param durationSeconds Duration of the work period
     * @param intensity Exercise intensity level
     * @return List of HR samples generated during this period
     */
    fun simulateWork(durationSeconds: Double, intensity: ExerciseIntensity): List<HRSample> {
        val targetHR = hrForIntensity(intensity)
        val newSamples = generateWorkCurve(currentHR, targetHR, durationSeconds)

        // Estimate steps based on intensity and duration
        val stepsPerMinute = when (intensity) {
            ExerciseIntensity.REST -> 0
            ExerciseIntensity.LOW -> 60
            ExerciseIntensity.MODERATE -> 100
            ExerciseIntensity.HIGH -> 140
        }
        totalSteps += ((stepsPerMinute * durationSeconds) / 60).toInt()

        return newSamples
    }

    /**
     * Simulate heart rate during a rest period.
     * HR decays exponentially toward resting HR.
     *
     * @param durationSeconds Duration of the rest period
     * @return List of HR samples generated during this period
     */
    fun simulateRest(durationSeconds: Double): List<HRSample> {
        return generateRecoveryCurve(currentHR, durationSeconds)
    }

    /**
     * Generate HR curve during work - exponential ramp up.
     */
    private fun generateWorkCurve(
        fromHR: Int,
        toHR: Int,
        durationSeconds: Double
    ): List<HRSample> {
        val newSamples = mutableListOf<HRSample>()
        val sampleIntervalSeconds = 5.0
        val sampleCount = (durationSeconds / sampleIntervalSeconds).toInt().coerceAtLeast(1)

        var currentTime = clock.now

        for (i in 0..sampleCount) {
            val progress = i.toDouble() / sampleCount
            // Exponential ramp: starts slow, accelerates
            val curve = 1 - (1 - progress).pow(2)
            val hr = fromHR + ((toHR - fromHR) * curve).toInt()

            // Add realistic noise (-3 to +3 BPM)
            val noisyHR = (hr + Random.nextInt(-3, 4))
                .coerceIn(profile.restingHR, profile.maxHR)

            newSamples.add(HRSample(timestamp = currentTime, value = noisyHR))
            currentHR = noisyHR
            currentTime = Instant.fromEpochMilliseconds(
                currentTime.toEpochMilliseconds() + (sampleIntervalSeconds * 1000).toLong()
            )
        }

        // Calculate calories burned (simplified formula)
        // Calories/min ≈ (age * 0.2017) + (weight * 0.1988) + (HR * 0.6309) − 55.0969
        // Simplified: HR * 0.1 * minutes
        val avgHR = (fromHR + toHR) / 2
        totalCalories += avgHR * 0.1 * (durationSeconds / 60.0)

        samples.addAll(newSamples)
        return newSamples
    }

    /**
     * Generate HR curve during rest - exponential decay.
     */
    private fun generateRecoveryCurve(fromHR: Int, durationSeconds: Double): List<HRSample> {
        val newSamples = mutableListOf<HRSample>()
        val sampleIntervalSeconds = 5.0
        val sampleCount = (durationSeconds / sampleIntervalSeconds).toInt().coerceAtLeast(1)

        var currentTime = clock.now
        var hr = fromHR.toDouble()

        for (i in 0..sampleCount) {
            // Exponential decay toward resting HR
            val decay = exp(-profile.recoveryRate * i * sampleIntervalSeconds / 60.0)
            hr = profile.restingHR + (fromHR - profile.restingHR) * decay

            // Add slight noise (-2 to +2 BPM)
            val noisyHR = (hr.toInt() + Random.nextInt(-2, 3))
                .coerceAtLeast(profile.restingHR)

            newSamples.add(HRSample(timestamp = currentTime, value = noisyHR))
            currentHR = noisyHR
            currentTime = Instant.fromEpochMilliseconds(
                currentTime.toEpochMilliseconds() + (sampleIntervalSeconds * 1000).toLong()
            )
        }

        samples.addAll(newSamples)
        return newSamples
    }

    /**
     * Get target HR based on exercise intensity.
     */
    private fun hrForIntensity(intensity: ExerciseIntensity): Int = when (intensity) {
        ExerciseIntensity.REST -> profile.restingHR + 10
        ExerciseIntensity.LOW -> profile.restingHR + 40
        ExerciseIntensity.MODERATE -> profile.restingHR + 70
        ExerciseIntensity.HIGH -> profile.maxHR - 10
    }

    /**
     * Get all collected health data for the workout.
     */
    fun getCollectedData(): SimulatedHealthData {
        val hrValues = samples.map { it.value }
        return SimulatedHealthData(
            hrSamples = samples.toList(),
            avgHR = if (hrValues.isNotEmpty()) hrValues.average().toInt() else 0,
            maxHR = hrValues.maxOrNull() ?: 0,
            minHR = hrValues.minOrNull() ?: profile.restingHR,
            calories = totalCalories.toInt(),
            steps = totalSteps
        )
    }

    /**
     * Reset all collected data (for starting a new workout).
     */
    fun reset() {
        currentHR = profile.restingHR
        samples.clear()
        totalCalories = 0.0
        totalSteps = 0
    }
}
