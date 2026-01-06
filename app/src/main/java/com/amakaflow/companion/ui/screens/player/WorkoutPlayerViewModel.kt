package com.amakaflow.companion.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amakaflow.companion.data.model.*
import com.amakaflow.companion.data.repository.Result
import com.amakaflow.companion.data.repository.WorkoutRepository
import com.amakaflow.companion.debug.DebugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject

private const val TAG = "Workout"

data class WorkoutPlayerUiState(
    val isLoading: Boolean = true,
    val workout: Workout? = null,
    val phase: WorkoutPhase = WorkoutPhase.IDLE,
    val currentStepIndex: Int = 0,
    val remainingSeconds: Int = 0,
    val elapsedSeconds: Int = 0,
    val flattenedSteps: List<FlattenedInterval> = emptyList(),
    val restRemainingSeconds: Int = 0,
    val isManualRest: Boolean = false,
    val error: String? = null,
    val showEndConfirmation: Boolean = false,
    val workoutCompleted: Boolean = false
) {
    val currentStep: FlattenedInterval?
        get() = flattenedSteps.getOrNull(currentStepIndex)

    val nextStep: FlattenedInterval?
        get() = flattenedSteps.getOrNull(currentStepIndex + 1)

    val progress: Float
        get() {
            if (flattenedSteps.isEmpty()) return 0f
            return (currentStepIndex + 1).toFloat() / flattenedSteps.size
        }

    val formattedElapsedTime: String
        get() {
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val secs = elapsedSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format("%d:%02d", minutes, secs)
            }
        }

    val formattedRemainingTime: String
        get() {
            val minutes = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format("%d:%02d", minutes, secs)
        }

    val formattedRestTime: String
        get() {
            val minutes = restRemainingSeconds / 60
            val secs = restRemainingSeconds % 60
            return String.format("%d:%02d", minutes, secs)
        }

    val isPlaying: Boolean
        get() = phase == WorkoutPhase.RUNNING

    val isPaused: Boolean
        get() = phase == WorkoutPhase.PAUSED

    val isResting: Boolean
        get() = phase == WorkoutPhase.RESTING

    val canGoBack: Boolean
        get() = currentStepIndex > 0

    val canGoForward: Boolean
        get() = currentStepIndex < flattenedSteps.size - 1
}

@HiltViewModel
class WorkoutPlayerViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String = savedStateHandle["workoutId"] ?: ""

    private val _uiState = MutableStateFlow(WorkoutPlayerUiState())
    val uiState: StateFlow<WorkoutPlayerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var workoutStartTime: Instant? = null

    init {
        loadWorkout()
    }

    private fun loadWorkout() {
        DebugLog.info("Loading workout: $workoutId", TAG)
        viewModelScope.launch {
            workoutRepository.getWorkout(workoutId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        val workout = result.data
                        val flattenedSteps = IntervalFlattener.flatten(workout.intervals)
                        DebugLog.success("Loaded workout: ${workout.name} (${flattenedSteps.size} steps)", TAG)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                workout = workout,
                                flattenedSteps = flattenedSteps,
                                error = null
                            )
                        }
                        // Auto-start the workout
                        start()
                    }
                    is Result.Error -> {
                        DebugLog.error("Failed to load workout: ${result.message}", TAG)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun start() {
        val currentState = _uiState.value
        if (currentState.flattenedSteps.isEmpty()) return

        DebugLog.info("Starting workout: ${currentState.workout?.name}", TAG)
        timerJob?.cancel()
        workoutStartTime = Clock.System.now()

        _uiState.update {
            it.copy(
                phase = WorkoutPhase.RUNNING,
                currentStepIndex = 0,
                remainingSeconds = 0,
                elapsedSeconds = 0
            )
        }

        setupCurrentStep()
    }

    fun pause() {
        if (_uiState.value.phase != WorkoutPhase.RUNNING) return
        DebugLog.debug("Workout paused", TAG)
        _uiState.update { it.copy(phase = WorkoutPhase.PAUSED) }
        timerJob?.cancel()
    }

    fun resume() {
        if (_uiState.value.phase != WorkoutPhase.PAUSED) return
        DebugLog.debug("Workout resumed", TAG)
        _uiState.update { it.copy(phase = WorkoutPhase.RUNNING) }
        startTimer()
    }

    fun togglePlayPause() {
        when (_uiState.value.phase) {
            WorkoutPhase.RUNNING -> pause()
            WorkoutPhase.PAUSED -> resume()
            else -> {}
        }
    }

    fun nextStep() {
        val currentState = _uiState.value
        val step = currentState.currentStep ?: return

        // Check if current step has rest after it (matches iOS behavior)
        if (step.hasRestAfter && currentState.phase != WorkoutPhase.RESTING) {
            // restAfterSeconds: null = manual rest, >0 = timed countdown
            enterRestPhase(step.restAfterSeconds)
            return
        }

        if (currentState.currentStepIndex >= currentState.flattenedSteps.size - 1) {
            end(EndReason.COMPLETED)
            return
        }

        _uiState.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
        setupCurrentStep()
    }

    fun previousStep() {
        if (_uiState.value.currentStepIndex <= 0) return
        _uiState.update { it.copy(currentStepIndex = it.currentStepIndex - 1) }
        setupCurrentStep()
    }

    fun skipRest() {
        if (_uiState.value.phase != WorkoutPhase.RESTING) return
        completeRest()
    }

    fun showEndConfirmation() {
        _uiState.update { it.copy(showEndConfirmation = true) }
    }

    fun hideEndConfirmation() {
        _uiState.update { it.copy(showEndConfirmation = false) }
    }

    fun endAndSave() {
        hideEndConfirmation()
        end(EndReason.USER_ENDED)
    }

    fun endAndDiscard() {
        hideEndConfirmation()
        end(EndReason.DISCARDED)
    }

    fun end(reason: EndReason) {
        val currentState = _uiState.value
        DebugLog.info("Ending workout: reason=$reason, elapsed=${currentState.elapsedSeconds}s", TAG)

        val workoutData = Triple(
            currentState.workout?.id,
            currentState.workout?.name,
            workoutStartTime
        )
        val duration = currentState.elapsedSeconds
        val intervals = currentState.workout?.intervals

        timerJob?.cancel()
        _uiState.update {
            it.copy(
                phase = WorkoutPhase.ENDED,
                workoutCompleted = reason == EndReason.COMPLETED || reason == EndReason.USER_ENDED
            )
        }

        // Post completion to API if completed or user ended
        if (reason == EndReason.COMPLETED || reason == EndReason.USER_ENDED) {
            DebugLog.info("Posting workout completion...", TAG)
            postWorkoutCompletion(
                workoutId = workoutData.first,
                workoutName = workoutData.second,
                startedAt = workoutData.third,
                durationSeconds = duration,
                intervals = intervals
            )
        } else {
            DebugLog.debug("Workout discarded, not posting completion", TAG)
        }
    }

    // Timer Management

    private fun setupCurrentStep() {
        timerJob?.cancel()

        val step = _uiState.value.currentStep ?: return

        if (step.durationSeconds != null) {
            _uiState.update { it.copy(remainingSeconds = step.durationSeconds!!) }
            if (_uiState.value.phase == WorkoutPhase.RUNNING) {
                startTimer()
            }
        } else {
            _uiState.update { it.copy(remainingSeconds = 0) }
            // For reps-based exercises, still run elapsed timer
            if (_uiState.value.phase == WorkoutPhase.RUNNING) {
                startElapsedOnlyTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                timerTick()
            }
        }
    }

    private fun startElapsedOnlyTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun timerTick() {
        val currentState = _uiState.value
        _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }

        if (currentState.remainingSeconds <= 0) {
            // For reps-based, don't auto-advance
            if (currentState.currentStep?.stepType == StepType.REPS) return
            nextStep()
            return
        }

        _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }

        if (_uiState.value.remainingSeconds == 0) {
            viewModelScope.launch {
                delay(500)
                nextStep()
            }
        }
    }

    // Rest Phase

    private fun enterRestPhase(restSeconds: Int?) {
        timerJob?.cancel()
        _uiState.update { it.copy(phase = WorkoutPhase.RESTING) }

        if (restSeconds != null && restSeconds > 0) {
            _uiState.update {
                it.copy(
                    isManualRest = false,
                    restRemainingSeconds = restSeconds
                )
            }
            startRestTimer()
        } else {
            _uiState.update {
                it.copy(
                    isManualRest = true,
                    restRemainingSeconds = 0
                )
            }
        }
    }

    private fun startRestTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.restRemainingSeconds > 0 && isActive) {
                delay(1000)
                _uiState.update {
                    it.copy(
                        restRemainingSeconds = it.restRemainingSeconds - 1,
                        elapsedSeconds = it.elapsedSeconds + 1
                    )
                }
            }
            if (_uiState.value.restRemainingSeconds == 0) {
                delay(500)
                completeRest()
            }
        }
    }

    private fun completeRest() {
        if (_uiState.value.phase != WorkoutPhase.RESTING) return

        timerJob?.cancel()
        _uiState.update {
            it.copy(
                restRemainingSeconds = 0,
                isManualRest = false
            )
        }

        val currentState = _uiState.value
        if (currentState.currentStepIndex >= currentState.flattenedSteps.size - 1) {
            end(EndReason.COMPLETED)
            return
        }

        _uiState.update {
            it.copy(
                currentStepIndex = it.currentStepIndex + 1,
                phase = WorkoutPhase.RUNNING
            )
        }
        setupCurrentStep()
    }

    // API

    private fun postWorkoutCompletion(
        workoutId: String?,
        workoutName: String?,
        startedAt: Instant?,
        durationSeconds: Int,
        intervals: List<WorkoutInterval>?
    ) {
        if (workoutId == null || startedAt == null) return

        viewModelScope.launch {
            try {
                val submission = WorkoutCompletionSubmission(
                    workoutId = workoutId,
                    workoutName = workoutName ?: "Workout",
                    startedAt = startedAt,
                    endedAt = Clock.System.now(),
                    source = CompletionSource.PHONE,
                    healthMetrics = HealthMetrics(
                        avgHeartRate = null,
                        maxHeartRate = null,
                        minHeartRate = null,
                        activeCalories = null,
                        totalCalories = null,
                        distanceMeters = null,
                        steps = null
                    ),
                    workoutStructure = intervals?.map { it.toSubmissionInterval() }
                )
                val result = workoutRepository.completeWorkout(submission)
                when (result) {
                    is Result.Success -> {
                        DebugLog.success("Workout completion posted: ${result.data.id}", TAG)
                    }
                    is Result.Error -> {
                        DebugLog.error("Failed to post completion: ${result.message}", TAG)
                    }
                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                DebugLog.error("Exception posting completion: ${e.message}", TAG)
                DebugLog.error(e, TAG)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
