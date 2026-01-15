package com.amakaflow.companion.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.amakaflow.companion.domain.sync.SyncCoordinator
import com.amakaflow.companion.domain.sync.SyncState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_WORK_NAME = "completion_sync"

@Singleton
class SyncCoordinatorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncCoordinator {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)

    override fun requestSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<CompletionSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun requestImmediateSync() {
        val request = OneTimeWorkRequestBuilder<CompletionSyncWorker>()
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )

        _syncState.value = SyncState.Syncing
    }

    override fun observeSyncState(): Flow<SyncState> {
        // Observe WorkManager state and convert to SyncState
        return workManager.getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { workInfos ->
                val workInfo = workInfos.firstOrNull()
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> SyncState.Syncing
                    WorkInfo.State.SUCCEEDED -> SyncState.Success(0) // We don't have access to synced count here
                    WorkInfo.State.FAILED -> SyncState.Failed("Sync failed")
                    WorkInfo.State.CANCELLED -> SyncState.Idle
                    WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncState.Idle
                    null -> SyncState.Idle
                }
            }
    }

    override fun cancelPendingSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        _syncState.value = SyncState.Idle
    }
}
