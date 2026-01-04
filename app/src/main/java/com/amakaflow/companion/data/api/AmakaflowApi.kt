package com.amakaflow.companion.data.api

import com.amakaflow.companion.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Amakaflow API interface for Retrofit
 */
interface AmakaflowApi {

    // MARK: - Pairing

    @POST("mobile/pairing/pair")
    suspend fun pair(@Body request: PairingRequest): Response<PairingResponse>

    @POST("mobile/pairing/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>

    // MARK: - Workouts

    @GET("workouts/incoming")
    suspend fun getIncomingWorkouts(): Response<List<Workout>>

    @GET("workouts/{id}")
    suspend fun getWorkout(@Path("id") id: String): Response<Workout>

    // MARK: - Activity History

    @GET("completions")
    suspend fun getCompletions(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<List<WorkoutCompletion>>

    @GET("completion-details/{id}")
    suspend fun getCompletionDetail(@Path("id") id: String): Response<WorkoutCompletionDetail>
}

/**
 * Ingestor API interface for workout completions
 */
interface IngestorApi {

    @POST("ios-companion/pending")
    suspend fun queueCompletion(@Body submission: WorkoutCompletionSubmission): Response<Unit>

    @POST("submit")
    suspend fun submitCompletion(@Body submission: WorkoutCompletionSubmission): Response<WorkoutCompletion>
}
