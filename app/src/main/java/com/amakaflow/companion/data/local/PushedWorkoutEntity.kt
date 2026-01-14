package com.amakaflow.companion.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Local storage status for pushed workouts
 */
enum class PushedWorkoutStatus {
    ACTIVE,      // Workout is available for use
    COMPLETED,   // Workout has been completed
    EXPIRED      // Workout was removed from server
}

/**
 * Room entity for locally cached pushed workouts.
 * Stores workouts fetched from the android-companion endpoint so they persist
 * after sync confirmation (server filters synced workouts from response).
 */
@Entity(tableName = "pushed_workouts")
data class PushedWorkoutEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val sport: String, // WorkoutSport enum stored as string

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,

    val description: String?,

    val source: String, // WorkoutSource enum stored as string

    @ColumnInfo(name = "source_url")
    val sourceUrl: String?,

    @ColumnInfo(name = "intervals_json")
    val intervalsJson: String, // Serialized List<WorkoutInterval>

    val status: String = PushedWorkoutStatus.ACTIVE.name,

    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "last_updated_at")
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@Dao
interface PushedWorkoutDao {
    /**
     * Get all active (non-completed, non-expired) workouts
     */
    @Query("SELECT * FROM pushed_workouts WHERE status = 'ACTIVE' ORDER BY fetched_at DESC")
    fun getActiveWorkouts(): Flow<List<PushedWorkoutEntity>>

    /**
     * Get all active workouts synchronously (for non-Flow usage)
     */
    @Query("SELECT * FROM pushed_workouts WHERE status = 'ACTIVE' ORDER BY fetched_at DESC")
    suspend fun getActiveWorkoutsSync(): List<PushedWorkoutEntity>

    /**
     * Get a specific workout by ID
     */
    @Query("SELECT * FROM pushed_workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: String): PushedWorkoutEntity?

    /**
     * Get a specific workout by ID as Flow
     */
    @Query("SELECT * FROM pushed_workouts WHERE id = :workoutId")
    fun getByIdFlow(workoutId: String): Flow<PushedWorkoutEntity?>

    /**
     * Insert or update a workout (upsert)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: PushedWorkoutEntity)

    /**
     * Insert or update multiple workouts
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(workouts: List<PushedWorkoutEntity>)

    /**
     * Mark a workout as completed
     */
    @Query("UPDATE pushed_workouts SET status = 'COMPLETED', completed_at = :completedAt, last_updated_at = :completedAt WHERE id = :workoutId")
    suspend fun markCompleted(workoutId: String, completedAt: Long = System.currentTimeMillis())

    /**
     * Mark a workout as synced (confirmed with server)
     */
    @Query("UPDATE pushed_workouts SET synced_at = :syncedAt, last_updated_at = :syncedAt WHERE id = :workoutId")
    suspend fun markSynced(workoutId: String, syncedAt: Long = System.currentTimeMillis())

    /**
     * Mark workouts as expired (no longer on server)
     */
    @Query("UPDATE pushed_workouts SET status = 'EXPIRED', last_updated_at = :now WHERE id NOT IN (:activeIds) AND status = 'ACTIVE'")
    suspend fun markExpiredExcept(activeIds: List<String>, now: Long = System.currentTimeMillis())

    /**
     * Delete a specific workout
     */
    @Query("DELETE FROM pushed_workouts WHERE id = :workoutId")
    suspend fun deleteById(workoutId: String)

    /**
     * Delete all completed workouts older than specified time
     */
    @Query("DELETE FROM pushed_workouts WHERE status = 'COMPLETED' AND completed_at < :olderThan")
    suspend fun deleteOldCompleted(olderThan: Long)

    /**
     * Delete all expired workouts older than specified time
     */
    @Query("DELETE FROM pushed_workouts WHERE status = 'EXPIRED' AND last_updated_at < :olderThan")
    suspend fun deleteOldExpired(olderThan: Long)

    /**
     * Get count of active workouts
     */
    @Query("SELECT COUNT(*) FROM pushed_workouts WHERE status = 'ACTIVE'")
    fun getActiveCount(): Flow<Int>

    /**
     * Get all workouts (for debugging)
     */
    @Query("SELECT * FROM pushed_workouts ORDER BY fetched_at DESC")
    suspend fun getAllSync(): List<PushedWorkoutEntity>
}
