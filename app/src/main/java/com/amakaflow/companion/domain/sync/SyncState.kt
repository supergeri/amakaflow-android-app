package com.amakaflow.companion.domain.sync

/**
 * Represents the current state of background sync operations.
 * Part of the domain layer.
 */
sealed class SyncState {
    /**
     * No sync operation in progress.
     */
    data object Idle : SyncState()

    /**
     * Sync operation is currently in progress.
     */
    data object Syncing : SyncState()

    /**
     * Sync completed successfully.
     * @param syncedCount Number of items that were synced.
     */
    data class Success(val syncedCount: Int) : SyncState()

    /**
     * Sync failed with an error.
     * @param error Description of the error.
     */
    data class Failed(val error: String) : SyncState()
}
