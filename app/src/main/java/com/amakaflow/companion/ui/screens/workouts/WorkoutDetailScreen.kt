package com.amakaflow.companion.ui.screens.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutInterval
import com.amakaflow.companion.data.model.WorkoutSport
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AmakaColors.background,
                    titleContentColor = AmakaColors.textPrimary,
                    navigationIconContentColor = AmakaColors.textPrimary
                )
            )
        },
        bottomBar = {
            uiState.workout?.let { workout ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AmakaColors.surface
                ) {
                    Button(
                        onClick = { onStartWorkout(workout.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AmakaSpacing.md.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmakaColors.accentGreen
                        ),
                        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                        Text(
                            text = "Start Workout",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        },
        containerColor = AmakaColors.background
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmakaColors.accentBlue)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = AmakaColors.textSecondary
                    )
                }
            }
            uiState.workout != null -> {
                WorkoutDetailContent(
                    workout = uiState.workout!!,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun WorkoutDetailContent(
    workout: Workout,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AmakaSpacing.md.dp),
        verticalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)
    ) {
        item {
            WorkoutHeader(workout = workout)
        }

        item {
            Text(
                text = "Workout Plan",
                style = MaterialTheme.typography.titleLarge,
                color = AmakaColors.textPrimary
            )
        }

        itemsIndexed(workout.intervals) { index, interval ->
            IntervalRow(
                index = index + 1,
                interval = interval
            )
        }

        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
        }
    }
}

@Composable
private fun WorkoutHeader(workout: Workout) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = workout.name,
            style = MaterialTheme.typography.headlineMedium,
            color = AmakaColors.textPrimary
        )
        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Chip(
                label = workout.sport.displayName,
                color = workout.sport.color
            )
            Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
            Text(
                text = workout.formattedDuration,
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
            Text(
                text = " • ",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textTertiary
            )
            Text(
                text = "${workout.intervalCount} exercises",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
        }
        workout.description?.let { description ->
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
    }
}

@Composable
private fun Chip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(AmakaCornerRadius.sm.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(
                horizontal = AmakaSpacing.sm.dp,
                vertical = AmakaSpacing.xs.dp
            )
        )
    }
}

@Composable
private fun IntervalRow(
    index: Int,
    interval: WorkoutInterval
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number
            Surface(
                modifier = Modifier.size(32.dp),
                color = AmakaColors.surfaceElevated,
                shape = RoundedCornerShape(AmakaCornerRadius.sm.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = AmakaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))

            // Interval content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = interval.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AmakaColors.textPrimary
                )
                Text(
                    text = interval.displayDetail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textSecondary
                )
            }

            // Icon
            Icon(
                imageVector = interval.icon,
                contentDescription = null,
                tint = interval.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Extension properties for WorkoutInterval
private val WorkoutInterval.displayName: String
    get() = when (this) {
        is WorkoutInterval.Warmup -> "Warm Up"
        is WorkoutInterval.Cooldown -> "Cool Down"
        is WorkoutInterval.Time -> target ?: "Timed Interval"
        is WorkoutInterval.Reps -> name
        is WorkoutInterval.Distance -> "Distance"
        is WorkoutInterval.Repeat -> "Repeat ${reps}x"
    }

private val WorkoutInterval.displayDetail: String
    get() = when (this) {
        is WorkoutInterval.Warmup -> formatTime(seconds)
        is WorkoutInterval.Cooldown -> formatTime(seconds)
        is WorkoutInterval.Time -> formatTime(seconds)
        is WorkoutInterval.Reps -> {
            val setsStr = if (sets != null && sets > 1) "${sets} × " else ""
            val loadStr = if (!load.isNullOrEmpty()) " @ $load" else ""
            "$setsStr$reps reps$loadStr"
        }
        is WorkoutInterval.Distance -> formatDistance(meters)
        is WorkoutInterval.Repeat -> "${intervals.size} exercises"
    }

private val WorkoutInterval.icon: ImageVector
    get() = when (this) {
        is WorkoutInterval.Warmup -> Icons.Filled.Whatshot
        is WorkoutInterval.Cooldown -> Icons.Filled.AcUnit
        is WorkoutInterval.Time -> Icons.Filled.Timer
        is WorkoutInterval.Reps -> Icons.Filled.FitnessCenter
        is WorkoutInterval.Distance -> Icons.Filled.DirectionsRun
        is WorkoutInterval.Repeat -> Icons.Filled.Refresh
    }

private val WorkoutInterval.iconColor: Color
    get() = when (this) {
        is WorkoutInterval.Warmup -> AmakaColors.accentOrange
        is WorkoutInterval.Cooldown -> AmakaColors.accentBlue
        is WorkoutInterval.Time -> AmakaColors.accentGreen
        is WorkoutInterval.Reps -> AmakaColors.accentPurple
        is WorkoutInterval.Distance -> AmakaColors.accentGreen
        is WorkoutInterval.Repeat -> AmakaColors.textSecondary
    }

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) {
        if (secs > 0) "${mins}m ${secs}s" else "${mins} min"
    } else {
        "${secs}s"
    }
}

private fun formatDistance(meters: Int): String {
    return if (meters >= 1000) {
        String.format("%.1f km", meters / 1000.0)
    } else {
        "${meters}m"
    }
}

// Extension for sport
private val WorkoutSport.color: Color
    get() = when (this) {
        WorkoutSport.RUNNING -> AmakaColors.sportRunning
        WorkoutSport.CYCLING -> AmakaColors.sportCycling
        WorkoutSport.STRENGTH -> AmakaColors.sportStrength
        WorkoutSport.MOBILITY -> AmakaColors.sportMobility
        WorkoutSport.SWIMMING -> AmakaColors.sportSwimming
        WorkoutSport.CARDIO -> AmakaColors.sportCardio
        WorkoutSport.OTHER -> AmakaColors.textSecondary
    }

private val WorkoutSport.displayName: String
    get() = when (this) {
        WorkoutSport.RUNNING -> "Running"
        WorkoutSport.CYCLING -> "Cycling"
        WorkoutSport.STRENGTH -> "Strength"
        WorkoutSport.MOBILITY -> "Mobility"
        WorkoutSport.SWIMMING -> "Swimming"
        WorkoutSport.CARDIO -> "Cardio"
        WorkoutSport.OTHER -> "Other"
    }
