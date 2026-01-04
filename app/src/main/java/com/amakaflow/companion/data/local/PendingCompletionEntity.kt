package com.amakaflow.companion.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pending_completions")
data class PendingCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "workout_id")
    val workoutId: String?,
    @ColumnInfo(name = "workout_name")
    val workoutName: String,
    @ColumnInfo(name = "started_at")
    val startedAt: String,
    @ColumnInfo(name = "ended_at")
    val endedAt: String?,
    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int,
    val source: String,
    @ColumnInfo(name = "avg_heart_rate")
    val avgHeartRate: Int?,
    @ColumnInfo(name = "max_heart_rate")
    val maxHeartRate: Int?,
    @ColumnInfo(name = "min_heart_rate")
    val minHeartRate: Int?,
    @ColumnInfo(name = "active_calories")
    val activeCalories: Int?,
    @ColumnInfo(name = "device_info_json")
    val deviceInfoJson: String?,
    @ColumnInfo(name = "workout_structure_json")
    val workoutStructureJson: String?,
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    @ColumnInfo(name = "last_error")
    val lastError: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PendingCompletionDao {
    @Query("SELECT * FROM pending_completions ORDER BY created_at ASC")
    fun getAll(): Flow<List<PendingCompletionEntity>>

    @Query("SELECT * FROM pending_completions ORDER BY created_at ASC LIMIT 1")
    suspend fun getOldest(): PendingCompletionEntity?

    @Query("SELECT COUNT(*) FROM pending_completions")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_completions")
    suspend fun getCountSync(): Int

    @Insert
    suspend fun insert(completion: PendingCompletionEntity): Long

    @Update
    suspend fun update(completion: PendingCompletionEntity)

    @Delete
    suspend fun delete(completion: PendingCompletionEntity)

    @Query("DELETE FROM pending_completions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_completions SET retry_count = retry_count + 1, last_error = :error WHERE id = :id")
    suspend fun incrementRetry(id: Long, error: String?)
}
