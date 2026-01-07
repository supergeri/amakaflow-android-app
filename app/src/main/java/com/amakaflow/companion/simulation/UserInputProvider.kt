package com.amakaflow.companion.simulation

import kotlinx.coroutines.CompletableDeferred
import kotlin.random.Random

/**
 * Interface for user input operations during workouts.
 * Allows swapping between real user input and simulated auto-advance.
 */
interface UserInputProvider {
    /**
     * Wait for the user to tap "Next" to proceed to the next step.
     */
    suspend fun waitForNextTap()

    /**
     * Wait for the user to input their completed reps.
     * @param target The target number of reps
     * @return The actual number of reps completed
     */
    suspend fun waitForRepsInput(target: Int): Int

    /**
     * Wait for the user to tap "Ready" after a rest period.
     */
    suspend fun waitForReadyTap()

    /**
     * Check if the workout should pause (e.g., user distraction).
     */
    fun shouldPause(): Boolean

    /**
     * Check if the current step should be skipped.
     */
    fun shouldSkip(): Boolean
}

/**
 * Production implementation - waits for real user taps.
 * Uses CompletableDeferred to suspend until user interaction.
 */
class RealUserInput : UserInputProvider {
    private var nextTapDeferred: CompletableDeferred<Unit>? = null
    private var repsDeferred: CompletableDeferred<Int>? = null
    private var readyTapDeferred: CompletableDeferred<Unit>? = null

    override suspend fun waitForNextTap() {
        nextTapDeferred = CompletableDeferred()
        nextTapDeferred?.await()
    }

    /**
     * Call this when the user taps the "Next" button.
     */
    fun userDidTapNext() {
        nextTapDeferred?.complete(Unit)
        nextTapDeferred = null
    }

    override suspend fun waitForRepsInput(target: Int): Int {
        repsDeferred = CompletableDeferred()
        return repsDeferred?.await() ?: target
    }

    /**
     * Call this when the user inputs their completed reps.
     */
    fun userDidInputReps(reps: Int) {
        repsDeferred?.complete(reps)
        repsDeferred = null
    }

    override suspend fun waitForReadyTap() {
        readyTapDeferred = CompletableDeferred()
        readyTapDeferred?.await()
    }

    /**
     * Call this when the user taps "Ready" after rest.
     */
    fun userDidTapReady() {
        readyTapDeferred?.complete(Unit)
        readyTapDeferred = null
    }

    override fun shouldPause(): Boolean = false
    override fun shouldSkip(): Boolean = false
}

/**
 * Simulation implementation - auto-advances with realistic delays.
 * Uses the behavior profile to determine timing and variance.
 *
 * @param clock The clock to use for delays (should be AcceleratedClock in simulation)
 * @param profile The user behavior profile determining timing patterns
 */
class SimulatedUserInput(
    private val clock: WorkoutClock,
    private val profile: UserBehaviorProfile
) : UserInputProvider {

    override suspend fun waitForNextTap() {
        // Simulate reaction time before tapping
        val delayMillis = (profile.reactionTime.random() * 1000).toLong()
        clock.delay(delayMillis)
    }

    override suspend fun waitForRepsInput(target: Int): Int {
        // Simulate time to input reps
        val delayMillis = (profile.reactionTime.random() * 1000).toLong()
        clock.delay(delayMillis)

        // Add slight variance to logged reps (-1, 0, or +1)
        val variance = Random.nextInt(-1, 2)
        return maxOf(1, target + variance)
    }

    override suspend fun waitForReadyTap() {
        // For manual rest, simulate the user taking some time before being ready
        val baseRestSeconds = 60.0 // Default rest if not specified
        val actualRestSeconds = baseRestSeconds * profile.restTimeMultiplier.random()
        clock.delay((actualRestSeconds * 1000).toLong())
    }

    override fun shouldPause(): Boolean {
        return Random.nextDouble() < profile.pauseProbability
    }

    override fun shouldSkip(): Boolean {
        return Random.nextDouble() < profile.skipProbability
    }

    /**
     * Simulate a pause event (user got distracted).
     * @return Duration of the pause in milliseconds
     */
    suspend fun simulatePause(): Long {
        val pauseDurationSeconds = profile.pauseDuration.random()
        val pauseMillis = (pauseDurationSeconds * 1000).toLong()
        clock.delay(pauseMillis)
        return pauseMillis
    }
}
