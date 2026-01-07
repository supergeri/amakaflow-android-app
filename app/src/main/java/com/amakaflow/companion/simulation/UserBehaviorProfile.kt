package com.amakaflow.companion.simulation

import kotlin.random.Random

/**
 * Profile defining simulated user behavior patterns.
 * Different profiles simulate different workout styles (efficient, casual, distracted).
 */
data class UserBehaviorProfile(
    val restTimeMultiplier: ClosedFloatingPointRange<Double>,
    val pauseProbability: Double,
    val pauseDuration: ClosedFloatingPointRange<Double>, // seconds
    val reactionTime: ClosedFloatingPointRange<Double>,  // seconds
    val skipProbability: Double,
    val hrProfile: HRProfile
) {
    companion object {
        /**
         * Efficient user - minimal rest, quick reactions, rarely pauses or skips.
         * Represents an experienced, focused athlete.
         */
        val EFFICIENT = UserBehaviorProfile(
            restTimeMultiplier = 0.9..1.0,
            pauseProbability = 0.05,
            pauseDuration = 5.0..15.0,
            reactionTime = 0.3..1.0,
            skipProbability = 0.02,
            hrProfile = HRProfile.ATHLETIC
        )

        /**
         * Casual user - normal rest times, occasional pauses.
         * Represents a typical gym-goer.
         */
        val CASUAL = UserBehaviorProfile(
            restTimeMultiplier = 1.0..1.5,
            pauseProbability = 0.15,
            pauseDuration = 15.0..90.0,
            reactionTime = 1.0..3.0,
            skipProbability = 0.1,
            hrProfile = HRProfile.AVERAGE
        )

        /**
         * Distracted user - extended rest, frequent pauses, slower reactions.
         * Represents someone multitasking or in a busy gym environment.
         */
        val DISTRACTED = UserBehaviorProfile(
            restTimeMultiplier = 1.2..2.5,
            pauseProbability = 0.3,
            pauseDuration = 30.0..180.0,
            reactionTime = 2.0..8.0,
            skipProbability = 0.15,
            hrProfile = HRProfile.AVERAGE
        )

        /**
         * Get a profile by name.
         */
        fun fromName(name: String): UserBehaviorProfile = when (name.lowercase()) {
            "efficient" -> EFFICIENT
            "distracted" -> DISTRACTED
            else -> CASUAL
        }
    }
}

/**
 * Heart rate profile for simulated health data generation.
 *
 * @param restingHR Resting heart rate in BPM
 * @param maxHR Maximum heart rate in BPM
 * @param recoveryRate How quickly HR drops during rest (BPM per second)
 */
data class HRProfile(
    val restingHR: Int,
    val maxHR: Int,
    val recoveryRate: Double
) {
    companion object {
        /**
         * Athletic profile - lower resting HR, higher max, faster recovery.
         */
        val ATHLETIC = HRProfile(
            restingHR = 55,
            maxHR = 185,
            recoveryRate = 1.5
        )

        /**
         * Average fitness profile.
         */
        val AVERAGE = HRProfile(
            restingHR = 70,
            maxHR = 175,
            recoveryRate = 1.0
        )

        /**
         * Create a custom profile.
         */
        fun custom(restingHR: Int, maxHR: Int): HRProfile = HRProfile(
            restingHR = restingHR,
            maxHR = maxHR,
            recoveryRate = 1.0
        )
    }
}

/**
 * Extension function to get a random value from a ClosedFloatingPointRange.
 */
fun ClosedFloatingPointRange<Double>.random(): Double {
    return start + Random.nextDouble() * (endInclusive - start)
}
