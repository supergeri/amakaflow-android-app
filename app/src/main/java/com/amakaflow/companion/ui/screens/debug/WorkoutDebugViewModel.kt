package com.amakaflow.companion.ui.screens.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.domain.Result
import com.amakaflow.companion.domain.usecase.workout.GetPushedWorkoutsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WorkoutDebugViewModel"

data class WorkoutDebugUiState(
    val isLoading: Boolean = false,
    val workouts: List<Workout> = emptyList(),
    val status: String = "No pending workouts",
    val error: String? = null,
    val apiUrl: String = ""
)

@HiltViewModel
class WorkoutDebugViewModel @Inject constructor(
    private val getPushedWorkouts: GetPushedWorkoutsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutDebugUiState(
        apiUrl = AppEnvironment.current.mapperApiUrl
    ))
    val uiState: StateFlow<WorkoutDebugUiState> = _uiState.asStateFlow()

    fun fetchWorkouts() {
        Log.d(TAG, "fetchWorkouts: Starting fetch, environment=${AppEnvironment.current.displayName}, url=${AppEnvironment.current.mapperApiUrl}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, status = "Fetching...", error = null) }

            getPushedWorkouts().collect { result ->
                Log.d(TAG, "fetchWorkouts: Got result: $result")
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, status = "Loading...") }
                    }
                    is Result.Success -> {
                        Log.d(TAG, "fetchWorkouts: Success! Found ${result.data.size} workouts")
                        result.data.forEach { workout ->
                            Log.d(TAG, "  - ${workout.name} (${workout.id})")
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                workouts = result.data,
                                status = if (result.data.isEmpty()) "No pending workouts" else "${result.data.size} workout(s) loaded",
                                error = null,
                                apiUrl = AppEnvironment.current.mapperApiUrl
                            )
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "fetchWorkouts: Error - ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                workouts = emptyList(),
                                status = "Error",
                                error = result.message,
                                apiUrl = AppEnvironment.current.mapperApiUrl
                            )
                        }
                    }
                }
            }
        }
    }

    fun addSampleWorkout() {
        // For testing UI without real API
        val sampleWorkout = Workout(
            id = "sample-${System.currentTimeMillis()}",
            name = "Sample Workout ${_uiState.value.workouts.size + 1}",
            sport = com.amakaflow.companion.data.model.WorkoutSport.RUNNING,
            duration = 1800,
            intervals = emptyList(),
            source = com.amakaflow.companion.data.model.WorkoutSource.AI
        )
        _uiState.update {
            val newWorkouts = it.workouts + sampleWorkout
            it.copy(
                workouts = newWorkouts,
                status = "${newWorkouts.size} workout(s) loaded"
            )
        }
    }

    fun generateDebugText(): String {
        val state = _uiState.value
        val sb = StringBuilder()
        sb.appendLine("=== WORKOUT DEBUG ===")
        sb.appendLine("Environment: ${AppEnvironment.current.displayName}")
        sb.appendLine("API URL: ${state.apiUrl}")
        sb.appendLine("Status: ${state.status}")
        state.error?.let { sb.appendLine("Error: $it") }
        sb.appendLine()

        for (workout in state.workouts) {
            sb.appendLine("WORKOUT: ${workout.name}")
            sb.appendLine("ID: ${workout.id}")
            sb.appendLine("Sport: ${workout.sport.name}")
            sb.appendLine("Duration: ${workout.duration}s")
            sb.appendLine()

            sb.appendLine("RAW INTERVALS:")
            for ((i, interval) in workout.intervals.withIndex()) {
                sb.appendLine(formatIntervalForCopy(i, interval))
            }
            sb.appendLine()
        }

        return sb.toString()
    }

    private fun formatIntervalForCopy(index: Int, interval: com.amakaflow.companion.data.model.WorkoutInterval): String {
        return when (interval) {
            is com.amakaflow.companion.data.model.WorkoutInterval.Warmup ->
                "[$index] WARMUP: ${interval.seconds}s, target=${interval.target ?: "none"}"
            is com.amakaflow.companion.data.model.WorkoutInterval.Cooldown ->
                "[$index] COOLDOWN: ${interval.seconds}s, target=${interval.target ?: "none"}"
            is com.amakaflow.companion.data.model.WorkoutInterval.Time ->
                "[$index] TIME: ${interval.seconds}s, target=${interval.target ?: "none"}"
            is com.amakaflow.companion.data.model.WorkoutInterval.Distance ->
                "[$index] DISTANCE: ${interval.meters}m, target=${interval.target ?: "none"}"
            is com.amakaflow.companion.data.model.WorkoutInterval.Reps ->
                "[$index] REPS: ${interval.sets ?: 1}x${interval.reps} ${interval.name}"
            is com.amakaflow.companion.data.model.WorkoutInterval.Repeat -> {
                val nested = interval.intervals.mapIndexed { i, int ->
                    "    ${formatIntervalForCopy(i, int)}"
                }.joinToString("\n")
                "[$index] REPEAT: ${interval.reps}x\n$nested"
            }
            is com.amakaflow.companion.data.model.WorkoutInterval.Rest ->
                "[$index] REST: ${interval.seconds?.let { "${it}s" } ?: "manual"}"
        }
    }
}
