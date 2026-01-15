package com.amakaflow.companion.di

import com.amakaflow.companion.data.repository.CompletionRepositoryImpl
import com.amakaflow.companion.data.repository.PairingRepositoryImpl
import com.amakaflow.companion.data.repository.WorkoutRepositoryImpl
import com.amakaflow.companion.data.sync.SyncCoordinatorImpl
import com.amakaflow.companion.domain.repository.CompletionRepository
import com.amakaflow.companion.domain.repository.PairingRepository
import com.amakaflow.companion.domain.repository.WorkoutRepository
import com.amakaflow.companion.domain.sync.SyncCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository and coordinator interfaces to their implementations.
 * This enables dependency injection of domain interfaces throughout the app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        impl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindPairingRepository(
        impl: PairingRepositoryImpl
    ): PairingRepository

    @Binds
    @Singleton
    abstract fun bindCompletionRepository(
        impl: CompletionRepositoryImpl
    ): CompletionRepository

    @Binds
    @Singleton
    abstract fun bindSyncCoordinator(
        impl: SyncCoordinatorImpl
    ): SyncCoordinator
}
