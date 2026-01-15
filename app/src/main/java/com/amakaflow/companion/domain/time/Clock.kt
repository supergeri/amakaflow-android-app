package com.amakaflow.companion.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Interface for time operations.
 * Part of the domain layer - allows for testing with fake clocks.
 */
interface Clock {
    /**
     * Get the current instant.
     */
    fun now(): Instant

    /**
     * Get the current time in milliseconds since epoch.
     */
    fun currentTimeMillis(): Long

    /**
     * Get today's date.
     */
    fun todayDate(): LocalDate
}
