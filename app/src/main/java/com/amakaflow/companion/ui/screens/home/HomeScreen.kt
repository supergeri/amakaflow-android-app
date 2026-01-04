package com.amakaflow.companion.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
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

@Composable
fun HomeScreen(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToWorkoutDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
            .padding(horizontal = AmakaSpacing.md.dp),
        verticalArrangement = Arrangement.spacedBy(AmakaSpacing.lg.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
            WelcomeHeader(userName = uiState.userName)
        }

        item {
            QuickActionButtons(
                onQuickStart = { /* TODO */ },
                onVoiceWorkout = { /* TODO */ }
            )
        }

        if (uiState.todayWorkouts.isNotEmpty()) {
            item {
                SectionHeader(title = "Today's Workouts")
            }
            item {
                TodayWorkoutsSection(
                    workouts = uiState.todayWorkouts,
                    onWorkoutClick = onNavigateToWorkoutDetail
                )
            }
        }

        item {
            WeeklyStatsCard(
                workoutCount = uiState.weeklyStats.workoutCount,
                totalDuration = uiState.weeklyStats.formattedDuration,
                totalCalories = uiState.weeklyStats.formattedCalories
            )
        }

        if (uiState.upcomingWorkouts.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Upcoming Workouts",
                    actionText = "See All",
                    onAction = onNavigateToWorkouts
                )
            }
            items(uiState.upcomingWorkouts.take(3)) { workout ->
                WorkoutListItem(
                    workout = workout,
                    onClick = { onNavigateToWorkoutDetail(workout.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
        }
    }
}

@Composable
private fun WelcomeHeader(userName: String?) {
    Column {
        Text(
            text = if (userName != null) "Hey, $userName" else "Welcome back",
            style = MaterialTheme.typography.headlineMedium,
            color = AmakaColors.textPrimary
        )
        Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))
        Text(
            text = "Ready to crush your workout?",
            style = MaterialTheme.typography.bodyLarge,
            color = AmakaColors.textSecondary
        )
    }
}

@Composable
private fun QuickActionButtons(
    onQuickStart: () -> Unit,
    onVoiceWorkout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)
    ) {
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.PlayArrow,
            label = "Quick Start",
            color = AmakaColors.accentGreen,
            onClick = onQuickStart
        )
        QuickActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Mic,
            label = "Voice Workout",
            color = AmakaColors.accentBlue,
            onClick = onVoiceWorkout
        )
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(AmakaCornerRadius.md.dp))
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AmakaSpacing.md.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = AmakaColors.textPrimary
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = AmakaColors.accentBlue
                )
            }
        }
    }
}

@Composable
private fun TodayWorkoutsSection(
    workouts: List<Workout>,
    onWorkoutClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)
    ) {
        items(workouts) { workout ->
            WorkoutCard(
                workout = workout,
                onClick = { onWorkoutClick(workout.id) }
            )
        }
    }
}

@Composable
private fun WorkoutCard(
    workout: Workout,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(AmakaCornerRadius.md.dp))
            .clickable(onClick = onClick),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = workout.sport.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                Text(
                    text = workout.sport.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = AmakaColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleMedium,
                color = AmakaColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
            Text(
                text = workout.formattedDuration,
                style = MaterialTheme.typography.bodySmall,
                color = AmakaColors.textTertiary
            )
        }
    }
}

@Composable
private fun WeeklyStatsCard(
    workoutCount: Int,
    totalDuration: String,
    totalCalories: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium,
                color = AmakaColors.textSecondary
            )
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = workoutCount.toString(), label = "Workouts")
                StatItem(value = totalDuration, label = "Duration")
                StatItem(value = totalCalories, label = "Calories")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = AmakaColors.accentBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AmakaColors.textTertiary
        )
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
                modifier = Modifier.size(44.dp),
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
                Text(
                    text = "${workout.formattedDuration} • ${workout.intervalCount} exercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textTertiary
                )
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
