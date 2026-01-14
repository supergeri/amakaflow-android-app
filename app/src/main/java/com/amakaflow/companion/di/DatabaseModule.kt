package com.amakaflow.companion.di

import android.content.Context
import androidx.room.Room
import com.amakaflow.companion.data.local.AppDatabase
import com.amakaflow.companion.data.local.PendingCompletionDao
import com.amakaflow.companion.data.local.PushedWorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "amakaflow_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePendingCompletionDao(database: AppDatabase): PendingCompletionDao {
        return database.pendingCompletionDao()
    }

    @Provides
    @Singleton
    fun providePushedWorkoutDao(database: AppDatabase): PushedWorkoutDao {
        return database.pushedWorkoutDao()
    }
}
