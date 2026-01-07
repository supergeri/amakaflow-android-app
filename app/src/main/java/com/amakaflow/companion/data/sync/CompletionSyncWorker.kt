package com.amakaflow.companion.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.amakaflow.companion.data.api.AmakaflowApi
import com.amakaflow.companion.data.local.PendingCompletionDao
import com.amakaflow.companion.data.model.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@HiltWorker
class CompletionSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: AmakaflowApi,
    private val pendingCompletionDao: PendingCompletionDao,
    private val json: Json
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CompletionSyncWorker"
        private const val MAX_RETRIES = 5
        private const val WORK_NAME = "completion_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<CompletionSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    request
                )
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<CompletionSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "${WORK_NAME}_periodic",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting completion sync")

        val pendingCount = pendingCompletionDao.getCountSync()
        if (pendingCount == 0) {
            Log.d(TAG, "No pending completions to sync")
            return Result.success()
        }

        Log.d(TAG, "Found $pendingCount pending completions")

        var syncedCount = 0
        var failedCount = 0

        // Process completions one at a time (oldest first)
        while (true) {
            val completion = pendingCompletionDao.getOldest() ?: break

            if (completion.retryCount >= MAX_RETRIES) {
                Log.w(TAG, "Completion ${completion.id} exceeded max retries, removing")
                pendingCompletionDao.deleteById(completion.id)
                failedCount++
                continue
            }

            try {
                val submission = createSubmission(completion)
                val response = api.completeWorkout(submission)

                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully synced completion ${completion.id}")
                    pendingCompletionDao.deleteById(completion.id)
                    syncedCount++
                } else {
                    val error = "HTTP ${response.code()}: ${response.errorBody()?.string()}"
                    Log.e(TAG, "Failed to sync completion ${completion.id}: $error")

                    if (response.code() in 400..499 && response.code() != 401) {
                        // Client error (except auth) - don't retry
                        Log.w(TAG, "Client error for completion ${completion.id}, removing")
                        pendingCompletionDao.deleteById(completion.id)
                        failedCount++
                    } else {
                        pendingCompletionDao.incrementRetry(completion.id, error)
                        failedCount++
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception syncing completion ${completion.id}", e)
                pendingCompletionDao.incrementRetry(completion.id, e.message)
                failedCount++

                // Stop processing on network errors
                if (e is java.net.UnknownHostException || e is java.net.SocketTimeoutException) {
                    break
                }
            }
        }

        Log.d(TAG, "Sync complete: $syncedCount synced, $failedCount failed")

        return if (failedCount > 0 && pendingCompletionDao.getCountSync() > 0) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    private fun createSubmission(entity: com.amakaflow.companion.data.local.PendingCompletionEntity): WorkoutCompletionSubmission {
        val deviceInfo = entity.deviceInfoJson?.let {
            try {
                json.decodeFromString<CompletionDeviceInfo>(it)
            } catch (e: Exception) {
                null
            }
        }

        val structure = entity.workoutStructureJson?.let {
            try {
                json.decodeFromString<List<WorkoutIntervalSubmission>>(it)
            } catch (e: Exception) {
                null
            }
        }

        return WorkoutCompletionSubmission(
            workoutId = entity.workoutId,
            workoutName = entity.workoutName,
            startedAt = Instant.parse(entity.startedAt),
            endedAt = entity.endedAt?.let { Instant.parse(it) },
            source = CompletionSource.valueOf(entity.source.uppercase()),
            healthMetrics = HealthMetrics(
                avgHeartRate = entity.avgHeartRate,
                maxHeartRate = entity.maxHeartRate,
                minHeartRate = entity.minHeartRate,
                activeCalories = entity.activeCalories,
                totalCalories = null,
                distanceMeters = null,
                steps = null
            ),
            deviceInfo = deviceInfo,
            workoutStructure = structure,
            isSimulated = entity.isSimulated
        )
    }
}
