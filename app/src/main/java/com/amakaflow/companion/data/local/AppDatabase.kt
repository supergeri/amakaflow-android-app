package com.amakaflow.companion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PendingCompletionEntity::class, PushedWorkoutEntity::class],
    version = 4,  // Added pushed_workouts table for local workout storage
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pendingCompletionDao(): PendingCompletionDao
    abstract fun pushedWorkoutDao(): PushedWorkoutDao
}
