package com.amakaflow.companion.test

import com.amakaflow.companion.data.model.CompletionSource
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.data.model.WorkoutInterval
import com.amakaflow.companion.data.model.WorkoutSource
import com.amakaflow.companion.data.model.WorkoutSport
import kotlinx.datetime.Instant

/**
 * Test fixtures with sample data for unit tests.
 */
object TestFixtures {

    val hiitWorkout = Workout(
        id = "workout-001",
        name = "HIIT Blast",
        description = "High intensity interval training",
        duration = 1800, // 30 minutes
        sport = WorkoutSport.CARDIO,
        source = WorkoutSource.AI,
        intervals = listOf(
            WorkoutInterval.Warmup(
                seconds = 300,
                target = "Zone 2"
            ),
            WorkoutInterval.Repeat(
                reps = 8,
                intervals = listOf(
                    WorkoutInterval.Time(
                        seconds = 30,
                        target = "Zone 4-5"
                    ),
                    WorkoutInterval.Rest(
                        seconds = 30
                    )
                )
            ),
            WorkoutInterval.Cooldown(
                seconds = 180,
                target = "Zone 1"
            )
        )
    )

    val strengthWorkout = Workout(
        id = "workout-002",
        name = "Upper Body Strength",
        description = "Focus on chest, back, shoulders",
        duration = 2700, // 45 minutes
        sport = WorkoutSport.STRENGTH,
        source = WorkoutSource.COACH,
        intervals = listOf(
            WorkoutInterval.Warmup(
                seconds = 300,
                target = null
            ),
            WorkoutInterval.Reps(
                sets = 4,
                reps = 10,
                name = "Bench Press",
                load = "70% 1RM",
                restSec = 90
            ),
            WorkoutInterval.Reps(
                sets = 4,
                reps = 12,
                name = "Dumbbell Rows",
                load = "Moderate",
                restSec = 60
            ),
            WorkoutInterval.Reps(
                sets = 3,
                reps = 15,
                name = "Shoulder Press",
                load = "Light to Moderate",
                restSec = 60
            ),
            WorkoutInterval.Cooldown(
                seconds = 180,
                target = null
            )
        )
    )

    val runningWorkout = Workout(
        id = "workout-003",
        name = "5K Tempo Run",
        description = "Build endurance with tempo intervals",
        duration = 2400, // 40 minutes
        sport = WorkoutSport.RUNNING,
        source = WorkoutSource.AMAKA,
        intervals = listOf(
            WorkoutInterval.Warmup(
                seconds = 600,
                target = "Easy pace"
            ),
            WorkoutInterval.Distance(
                meters = 1000,
                target = "Tempo pace"
            ),
            WorkoutInterval.Rest(
                seconds = 120
            ),
            WorkoutInterval.Distance(
                meters = 1000,
                target = "Tempo pace"
            ),
            WorkoutInterval.Cooldown(
                seconds = 600,
                target = "Easy pace"
            )
        )
    )

    val sampleWorkouts = listOf(hiitWorkout, strengthWorkout, runningWorkout)

    val sampleCompletion = WorkoutCompletion(
        id = "completion-001",
        workoutId = hiitWorkout.id,
        workoutName = hiitWorkout.name,
        startedAt = Instant.parse("2024-01-15T10:00:00Z"),
        endedAt = Instant.parse("2024-01-15T10:30:00Z"),
        durationSeconds = 1800,
        avgHeartRate = 145,
        maxHeartRate = 178,
        minHeartRate = 85,
        activeCalories = 320,
        totalCalories = 380,
        source = CompletionSource.PHONE,
        originalWorkout = hiitWorkout
    )

    val strengthCompletion = WorkoutCompletion(
        id = "completion-002",
        workoutId = strengthWorkout.id,
        workoutName = strengthWorkout.name,
        startedAt = Instant.parse("2024-01-14T16:00:00Z"),
        endedAt = Instant.parse("2024-01-14T16:45:00Z"),
        durationSeconds = 2700,
        avgHeartRate = 110,
        maxHeartRate = 135,
        minHeartRate = 75,
        activeCalories = 250,
        totalCalories = 300,
        source = CompletionSource.PHONE,
        originalWorkout = strengthWorkout
    )

    val runningCompletion = WorkoutCompletion(
        id = "completion-003",
        workoutId = runningWorkout.id,
        workoutName = runningWorkout.name,
        startedAt = Instant.parse("2024-01-13T07:00:00Z"),
        endedAt = Instant.parse("2024-01-13T07:40:00Z"),
        durationSeconds = 2400,
        avgHeartRate = 155,
        maxHeartRate = 172,
        minHeartRate = 95,
        activeCalories = 400,
        totalCalories = 450,
        distanceMeters = 5200,
        steps = 5800,
        source = CompletionSource.APPLE_WATCH,
        originalWorkout = runningWorkout
    )

    val sampleCompletions = listOf(sampleCompletion, strengthCompletion, runningCompletion)

    // Empty workout with no intervals (for edge case testing)
    val emptyWorkout = Workout(
        id = "workout-empty",
        name = "Empty Workout",
        description = null,
        duration = 0,
        sport = WorkoutSport.OTHER,
        source = WorkoutSource.OTHER,
        intervals = emptyList()
    )

    // Minimal completion (for edge case testing)
    val minimalCompletion = WorkoutCompletion(
        id = "completion-minimal",
        workoutName = "Quick Session",
        startedAt = Instant.parse("2024-01-15T12:00:00Z"),
        durationSeconds = 300,
        source = CompletionSource.MANUAL
    )
}
