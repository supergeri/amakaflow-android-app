package com.amakaflow.companion.data.local

import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutInterval
import com.amakaflow.companion.data.model.WorkoutSource
import com.amakaflow.companion.data.model.WorkoutSport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Mapper for converting between Workout domain model and PushedWorkoutEntity Room entity.
 */
object WorkoutEntityMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Convert a Workout to a PushedWorkoutEntity for local storage.
     */
    fun toEntity(workout: Workout): PushedWorkoutEntity {
        return PushedWorkoutEntity(
            id = workout.id,
            name = workout.name,
            sport = WorkoutSport.toApiString(workout.sport),
            durationSeconds = workout.duration,
            description = workout.description,
            source = WorkoutSource.toApiString(workout.source),
            sourceUrl = workout.sourceUrl,
            intervalsJson = json.encodeToString(workout.intervals),
            status = PushedWorkoutStatus.ACTIVE.name,
            fetchedAt = System.currentTimeMillis()
        )
    }

    /**
     * Convert a list of Workouts to entities.
     */
    fun toEntities(workouts: List<Workout>): List<PushedWorkoutEntity> {
        return workouts.map { toEntity(it) }
    }

    /**
     * Convert a PushedWorkoutEntity back to a Workout domain model.
     */
    fun toWorkout(entity: PushedWorkoutEntity): Workout {
        val intervals: List<WorkoutInterval> = try {
            json.decodeFromString(entity.intervalsJson)
        } catch (e: Exception) {
            emptyList()
        }

        return Workout(
            id = entity.id,
            name = entity.name,
            sport = WorkoutSport.fromString(entity.sport),
            duration = entity.durationSeconds,
            intervals = intervals,
            description = entity.description,
            source = WorkoutSource.fromString(entity.source),
            sourceUrl = entity.sourceUrl
        )
    }

    /**
     * Convert a list of entities to Workouts.
     */
    fun toWorkouts(entities: List<PushedWorkoutEntity>): List<Workout> {
        return entities.map { toWorkout(it) }
    }
}
