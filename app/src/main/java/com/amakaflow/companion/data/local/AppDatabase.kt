package com.amakaflow.companion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PendingCompletionEntity::class],
    version = 3,  // Added is_simulated column
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingCompletionDao(): PendingCompletionDao
}
