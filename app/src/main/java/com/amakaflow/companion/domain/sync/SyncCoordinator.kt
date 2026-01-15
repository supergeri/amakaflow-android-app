package com.amakaflow.companion.domain.sync

import kotlinx.coroutines.flow.Flow

/**
 * Interface for coordinating background sync operations.
 * Part of the domain layer - implementation provided in data layer.
 */
interface SyncCoordinator {
    /**
     * Request a sync to be scheduled.
     * The sync may be debounced or delayed based on implementation.
     */
    fun requestSync()

    /**
     * Request an immediate sync operation.
     * Bypasses any debouncing or scheduling delays.
     */
    fun requestImmediateSync()

    /**
     * Observe the current sync state.
     */
    fun observeSyncState(): Flow<SyncState>

    /**
     * Cancel any pending sync operations.
     */
    fun cancelPendingSync()
}
