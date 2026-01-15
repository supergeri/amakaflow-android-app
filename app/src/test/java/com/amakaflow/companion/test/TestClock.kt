package com.amakaflow.companion.test

import com.amakaflow.companion.domain.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Test implementation of Clock that can be controlled in tests.
 */
class TestClock(
    private var currentInstant: Instant = Instant.parse("2024-01-15T10:00:00Z")
) : Clock {

    override fun now(): Instant = currentInstant

    override fun currentTimeMillis(): Long = currentInstant.toEpochMilliseconds()

    override fun todayDate(): LocalDate =
        TimeZone.UTC.let { tz ->
            currentInstant.toLocalDateTime(tz).date
        }

    // Test helpers
    fun setTime(instant: Instant) {
        currentInstant = instant
    }

    fun setTime(isoString: String) {
        currentInstant = Instant.parse(isoString)
    }

    fun advanceBy(millis: Long) {
        currentInstant = Instant.fromEpochMilliseconds(
            currentInstant.toEpochMilliseconds() + millis
        )
    }

    fun advanceBySeconds(seconds: Int) {
        advanceBy(seconds * 1000L)
    }

    fun advanceByMinutes(minutes: Int) {
        advanceBy(minutes * 60 * 1000L)
    }

    fun advanceByHours(hours: Int) {
        advanceBy(hours * 60 * 60 * 1000L)
    }

    fun advanceByDays(days: Int) {
        advanceBy(days * 24 * 60 * 60 * 1000L)
    }
}
