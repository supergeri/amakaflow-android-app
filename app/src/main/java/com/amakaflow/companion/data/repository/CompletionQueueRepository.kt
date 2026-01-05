package com.amakaflow.companion.data.repository

import android.content.Context
import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.local.PendingCompletionDao
import com.amakaflow.companion.data.local.PendingCompletionEntity
import com.amakaflow.companion.data.model.*
import com.amakaflow.companion.data.sync.CompletionSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompletionQueueRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: AmakaflowApi,
    private val pendingCompletionDao: PendingCompletionDao,
    private val json: Json
) {
    val pendingCount: Flow<Int> = pendingCompletionDao.getCount()
    val pendingCompletions: Flow<List<PendingCompletionEntity>> = pendingCompletionDao.getAll()

    /**
     * Submit a workout completion.
     * If online, attempts immediate submission. If offline or failed, queues for later sync.
     */
    suspend fun submitCompletion(submission: WorkoutCompletionSubmission): Result<WorkoutCompletion> {
        return try {
            // Try immediate submission
            val response = api.completeWorkout(submission)

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                // Queue for later if server error
                queueCompletion(submission)
                Result.Error("Submission queued for later sync", response.code())
            }
        } catch (e: Exception) {
            // Queue for later on network error
            queueCompletion(submission)
            Result.Error("No network - submission queued for later sync")
        }
    }

    /**
     * Queue a completion for later sync
     */
    suspend fun queueCompletion(submission: WorkoutCompletionSubmission) {
        // Calculate duration from startedAt and endedAt
        val durationSeconds = submission.endedAt?.let { ended ->
            ((ended.toEpochMilliseconds() - submission.startedAt.toEpochMilliseconds()) / 1000).toInt()
        } ?: 0

        val entity = PendingCompletionEntity(
            workoutId = submission.workoutId,
            workoutName = submission.workoutName,
            startedAt = submission.startedAt.toString(),
            endedAt = submission.endedAt?.toString(),
            durationSeconds = durationSeconds,
            source = submission.source.name.lowercase(),
            avgHeartRate = submission.healthMetrics.avgHeartRate,
            maxHeartRate = submission.healthMetrics.maxHeartRate,
            minHeartRate = submission.healthMetrics.minHeartRate,
            activeCalories = submission.healthMetrics.activeCalories,
            totalCalories = submission.healthMetrics.totalCalories,
            deviceInfoJson = submission.deviceInfo?.let { json.encodeToString(it) },
            workoutStructureJson = submission.workoutStructure?.let { json.encodeToString(it) }
        )

        pendingCompletionDao.insert(entity)

        // Trigger sync worker
        CompletionSyncWorker.enqueue(context)
    }

    /**
     * Manually trigger sync of pending completions
     */
    fun triggerSync() {
        CompletionSyncWorker.enqueue(context)
    }

    /**
     * Schedule periodic sync
     */
    fun schedulePeriodicSync() {
        CompletionSyncWorker.schedulePeriodicSync(context)
    }

    /**
     * Get count of pending completions synchronously
     */
    suspend fun getPendingCountSync(): Int = pendingCompletionDao.getCountSync()
}
