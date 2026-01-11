package com.amakaflow.companion.simulation

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * AMA-308: Weight profile for simulation - determines weight ranges for exercises.
 */
enum class WeightProfile {
    BEGINNER,      // Lower weight ranges - new to lifting
    INTERMEDIATE,  // Standard ranges - regular gym-goer
    ADVANCED;      // Higher ranges - experienced lifter

    companion object {
        fun fromName(name: String): WeightProfile = when (name.lowercase()) {
            "beginner" -> BEGINNER
            "advanced" -> ADVANCED
            else -> INTERMEDIATE
        }
    }
}

/**
 * AMA-308: Exercise type classification for weight selection.
 */
enum class ExerciseType {
    BARBELL_COMPOUND,  // Squat, Deadlift - heavier
    BARBELL_UPPER,     // Bench Press, OHP - moderate
    DUMBBELL,          // Per-hand exercises
    CABLE,             // Cable machine exercises
    MACHINE,           // Weight stack machines
    KETTLEBELL,        // Kettlebell exercises
    BODYWEIGHT,        // No external weight needed
    UNKNOWN            // Fallback
}

/**
 * AMA-308: Weight range for an exercise type at a given profile level.
 * Values are in lbs, will be converted if kg is selected.
 */
data class WeightRange(
    val min: Double,
    val max: Double
) {
    /**
     * Get a random weight within this range, with ±10% variance.
     */
    fun randomWeight(): Double {
        val midpoint = (min + max) / 2
        val variance = midpoint * 0.1 * (Random.nextDouble() * 2 - 1) // ±10%
        val weight = midpoint + variance
        // Round to nearest 5 (standard weight increment)
        return (weight / 5).roundToInt() * 5.0
    }
}

/**
 * AMA-308: Generates realistic simulated weights for strength exercises.
 * Uses exercise name classification and user's strength profile.
 */
class SimulatedWeightProvider(
    private val profile: WeightProfile
) {
    companion object {
        private const val LBS_TO_KG = 0.453592

        /**
         * Weight ranges by exercise type and profile (in lbs).
         */
        private val WEIGHT_RANGES: Map<ExerciseType, Map<WeightProfile, WeightRange>> = mapOf(
            ExerciseType.BARBELL_COMPOUND to mapOf(
                WeightProfile.BEGINNER to WeightRange(95.0, 135.0),
                WeightProfile.INTERMEDIATE to WeightRange(185.0, 275.0),
                WeightProfile.ADVANCED to WeightRange(315.0, 495.0)
            ),
            ExerciseType.BARBELL_UPPER to mapOf(
                WeightProfile.BEGINNER to WeightRange(65.0, 95.0),
                WeightProfile.INTERMEDIATE to WeightRange(135.0, 185.0),
                WeightProfile.ADVANCED to WeightRange(225.0, 315.0)
            ),
            ExerciseType.DUMBBELL to mapOf(
                WeightProfile.BEGINNER to WeightRange(15.0, 25.0),
                WeightProfile.INTERMEDIATE to WeightRange(35.0, 50.0),
                WeightProfile.ADVANCED to WeightRange(60.0, 100.0)
            ),
            ExerciseType.CABLE to mapOf(
                WeightProfile.BEGINNER to WeightRange(30.0, 50.0),
                WeightProfile.INTERMEDIATE to WeightRange(60.0, 90.0),
                WeightProfile.ADVANCED to WeightRange(100.0, 150.0)
            ),
            ExerciseType.MACHINE to mapOf(
                WeightProfile.BEGINNER to WeightRange(50.0, 80.0),
                WeightProfile.INTERMEDIATE to WeightRange(100.0, 150.0),
                WeightProfile.ADVANCED to WeightRange(180.0, 250.0)
            ),
            ExerciseType.KETTLEBELL to mapOf(
                WeightProfile.BEGINNER to WeightRange(25.0, 35.0),
                WeightProfile.INTERMEDIATE to WeightRange(45.0, 60.0),
                WeightProfile.ADVANCED to WeightRange(70.0, 100.0)
            )
        )

        /**
         * Default weight range for unknown exercises.
         */
        private val DEFAULT_RANGES = mapOf(
            WeightProfile.BEGINNER to WeightRange(30.0, 50.0),
            WeightProfile.INTERMEDIATE to WeightRange(60.0, 90.0),
            WeightProfile.ADVANCED to WeightRange(100.0, 140.0)
        )
    }

    /**
     * Get a simulated weight for an exercise.
     *
     * @param exerciseName The name of the exercise (e.g., "Bench Press", "Squat")
     * @param unit Weight unit ("lbs" or "kg")
     * @return Simulated weight with realistic variance, or null for bodyweight exercises
     */
    fun getSimulatedWeight(exerciseName: String, unit: String): Double? {
        val exerciseType = classifyExercise(exerciseName)

        // Bodyweight exercises don't need weight
        if (exerciseType == ExerciseType.BODYWEIGHT) {
            return null
        }

        val range = WEIGHT_RANGES[exerciseType]?.get(profile)
            ?: DEFAULT_RANGES[profile]
            ?: WeightRange(50.0, 80.0)

        val weightLbs = range.randomWeight()

        return if (unit.lowercase() == "kg") {
            // Convert to kg and round to nearest 2.5 (standard kg plate increment)
            val weightKg = weightLbs * LBS_TO_KG
            (weightKg / 2.5).roundToInt() * 2.5
        } else {
            weightLbs
        }
    }

    /**
     * Classify an exercise by name to determine appropriate weight range.
     */
    fun classifyExercise(name: String): ExerciseType {
        val normalized = name.lowercase().trim()

        return when {
            // Barbell compound movements (heavy lower body and full-body lifts)
            normalized.contains("squat") && !normalized.contains("goblet") ||
            normalized.contains("deadlift") ||
            normalized.contains("clean") ||
            normalized.contains("snatch") -> ExerciseType.BARBELL_COMPOUND

            // Barbell upper body movements
            normalized.contains("barbell") ||
            normalized.contains("bench press") ||
            normalized.contains("overhead press") ||
            normalized.contains("ohp") ||
            normalized.contains("military press") ||
            (normalized.contains("row") && !normalized.contains("dumbbell") && !normalized.contains("cable")) -> ExerciseType.BARBELL_UPPER

            // Dumbbell exercises
            normalized.contains("dumbbell") ||
            normalized.contains("db ") ||
            normalized.startsWith("db") ||
            normalized.contains("curl") && !normalized.contains("cable") ||
            normalized.contains("lateral raise") ||
            normalized.contains("fly") && !normalized.contains("cable") ||
            normalized.contains("kickback") ||
            normalized.contains("hammer") ||
            normalized.contains("concentration") -> ExerciseType.DUMBBELL

            // Cable exercises
            normalized.contains("cable") ||
            normalized.contains("pulldown") ||
            normalized.contains("tricep pushdown") ||
            normalized.contains("face pull") ||
            normalized.contains("crossover") -> ExerciseType.CABLE

            // Machine exercises
            normalized.contains("machine") ||
            normalized.contains("leg press") ||
            normalized.contains("chest press") && !normalized.contains("dumbbell") ||
            normalized.contains("leg curl") ||
            normalized.contains("leg extension") ||
            normalized.contains("seated row") ||
            normalized.contains("hack squat") ||
            normalized.contains("smith") -> ExerciseType.MACHINE

            // Kettlebell exercises
            normalized.contains("kettlebell") ||
            normalized.contains("kb ") ||
            normalized.startsWith("kb") ||
            normalized.contains("swing") ||
            normalized.contains("goblet") ||
            normalized.contains("turkish get") -> ExerciseType.KETTLEBELL

            // Bodyweight exercises - no weight needed
            normalized.contains("push-up") ||
            normalized.contains("pushup") ||
            normalized.contains("pull-up") ||
            normalized.contains("pullup") ||
            normalized.contains("chin-up") ||
            normalized.contains("chinup") ||
            normalized.contains("dip") && !normalized.contains("weight") ||
            normalized.contains("plank") ||
            normalized.contains("crunch") ||
            normalized.contains("sit-up") ||
            normalized.contains("situp") ||
            normalized.contains("burpee") ||
            normalized.contains("mountain climber") ||
            normalized.contains("jumping jack") ||
            normalized.contains("bodyweight") -> ExerciseType.BODYWEIGHT

            else -> ExerciseType.UNKNOWN
        }
    }
}
