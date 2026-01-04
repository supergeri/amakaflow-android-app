package com.amakaflow.companion.ui.screens.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutSport
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    onNavigateToWorkoutDetail: (String) -> Unit,
    viewModel: WorkoutsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            Text(
                text = "Workouts",
                style = MaterialTheme.typography.headlineMedium,
                color = AmakaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search workouts...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = AmakaColors.textSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AmakaColors.surface,
                    unfocusedContainerColor = AmakaColors.surface,
                    focusedBorderColor = AmakaColors.accentBlue,
                    unfocusedBorderColor = AmakaColors.borderLight,
                    focusedTextColor = AmakaColors.textPrimary,
                    unfocusedTextColor = AmakaColors.textPrimary,
                    cursorColor = AmakaColors.accentBlue
                ),
                shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
                singleLine = true
            )
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AmakaColors.accentBlue)
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AmakaColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmakaColors.accentBlue
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.workouts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No workouts found" else "No workouts available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AmakaColors.textSecondary
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AmakaSpacing.md.dp,
                        vertical = AmakaSpacing.sm.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
                ) {
                    items(uiState.workouts) { workout ->
                        WorkoutListItem(
                            workout = workout,
                            onClick = { onNavigateToWorkoutDetail(workout.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutListItem(
    workout: Workout,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AmakaCornerRadius.md.dp))
            .clickable(onClick = onClick),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = workout.sport.color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AmakaCornerRadius.sm.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = workout.sport.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AmakaColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workout.sport.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = workout.sport.color
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textTertiary
                    )
                    Text(
                        text = workout.formattedDuration,
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textTertiary
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textTertiary
                    )
                    Text(
                        text = "${workout.intervalCount} exercises",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textTertiary
                    )
                }
            }
        }
    }
}

// Extension to get sport color
private val WorkoutSport.color: androidx.compose.ui.graphics.Color
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
