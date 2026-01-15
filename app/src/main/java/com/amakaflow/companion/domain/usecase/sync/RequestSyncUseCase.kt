package com.amakaflow.companion.domain.usecase.sync

import com.amakaflow.companion.domain.sync.SyncCoordinator
import com.amakaflow.companion.domain.sync.SyncState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for requesting sync of pending data.
 */
class RequestSyncUseCase @Inject constructor(
    private val syncCoordinator: SyncCoordinator
) {
    /**
     * Request a sync to be scheduled.
     * The sync may be debounced or delayed based on implementation.
     */
    operator fun invoke() {
        syncCoordinator.requestSync()
    }

    /**
     * Request an immediate sync operation.
     * Bypasses any debouncing or scheduling delays.
     */
    fun immediate() {
        syncCoordinator.requestImmediateSync()
    }

    /**
     * Observe the current sync state.
     */
    fun observeState(): Flow<SyncState> {
        return syncCoordinator.observeSyncState()
    }
}
