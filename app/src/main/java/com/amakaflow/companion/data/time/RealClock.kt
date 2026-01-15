package com.amakaflow.companion.data.time

import com.amakaflow.companion.domain.time.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import javax.inject.Inject

/**
 * Real implementation of Clock using system time.
 */
class RealClock @Inject constructor() : Clock {
    override fun now(): Instant {
        return java.time.Instant.now().toKotlinInstant()
    }

    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun todayDate(): LocalDate {
        return java.time.LocalDate.now().toKotlinLocalDate()
    }
}
