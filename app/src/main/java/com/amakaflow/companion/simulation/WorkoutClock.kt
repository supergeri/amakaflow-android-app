package com.amakaflow.companion.simulation

import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Interface for time operations in workout tracking.
 * Allows swapping between real time and accelerated simulation time.
 */
interface WorkoutClock {
    val now: Instant
    suspend fun delay(durationMillis: Long)
}

/**
 * Production clock - uses real system time.
 * This is the default implementation for normal workout operation.
 */
class RealClock : WorkoutClock {
    override val now: Instant get() = Clock.System.now()

    override suspend fun delay(durationMillis: Long) {
        kotlinx.coroutines.delay(durationMillis)
    }
}

/**
 * Accelerated clock for simulation mode.
 * Runs at a configurable speed multiplier (e.g., 10x, 30x, 60x).
 *
 * @param speedMultiplier How much faster than real-time to run (e.g., 10.0 = 10x speed)
 * @param startTime The initial virtual time (defaults to current system time)
 */
class AcceleratedClock(
    private val speedMultiplier: Double,
    startTime: Instant = Clock.System.now()
) : WorkoutClock {
    private var virtualTime: Instant = startTime

    override val now: Instant get() = virtualTime

    override suspend fun delay(durationMillis: Long) {
        // Convert virtual duration to real delay based on speed multiplier
        val realDelayMillis = (durationMillis / speedMultiplier).toLong().coerceAtLeast(1)
        kotlinx.coroutines.delay(realDelayMillis)
        // Advance virtual time by the full duration
        virtualTime = Instant.fromEpochMilliseconds(virtualTime.toEpochMilliseconds() + durationMillis)
    }
}
