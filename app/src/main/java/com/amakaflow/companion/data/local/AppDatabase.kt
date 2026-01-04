package com.amakaflow.companion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PendingCompletionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingCompletionDao(): PendingCompletionDao
}
