package com.amakaflow.companion.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amakaflow.companion.data.model.CompletionSource
import com.amakaflow.companion.data.model.DateCategory
import com.amakaflow.companion.data.model.WorkoutCompletion
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@Composable
fun HistoryScreen(
    onNavigateToCompletionDetail: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Activity History",
                style = MaterialTheme.typography.headlineMedium,
                color = AmakaColors.textPrimary
            )
        }

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
            uiState.groupedCompletions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No workouts yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = AmakaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
                        Text(
                            text = "Complete a workout to see it here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textSecondary
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = AmakaSpacing.md.dp),
                    verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
                ) {
                    uiState.groupedCompletions.forEach { (category, completions) ->
                        item {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = AmakaColors.textSecondary,
                                modifier = Modifier.padding(
                                    top = AmakaSpacing.md.dp,
                                    bottom = AmakaSpacing.sm.dp
                                )
                            )
                        }
                        items(completions) { completion ->
                            CompletionListItem(
                                completion = completion,
                                onClick = { onNavigateToCompletionDetail(completion.id) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionListItem(
    completion: WorkoutCompletion,
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
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = completion.workoutName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AmakaColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Watch,
                        contentDescription = null,
                        tint = AmakaColors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = completion.source.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AmakaColors.textTertiary
                    )
                }
            }
            Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.lg.dp)
            ) {
                // Duration
                Column {
                    Text(
                        text = completion.formattedDuration,
                        style = MaterialTheme.typography.titleMedium,
                        color = AmakaColors.accentBlue
                    )
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmakaColors.textTertiary
                    )
                }

                // Heart Rate
                completion.avgHeartRate?.let { hr ->
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = AmakaColors.accentRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$hr",
                                style = MaterialTheme.typography.titleMedium,
                                color = AmakaColors.textPrimary
                            )
                        }
                        Text(
                            text = "Avg HR",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmakaColors.textTertiary
                        )
                    }
                }

                // Calories
                completion.activeCalories?.let { calories ->
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmakaColors.accentOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$calories",
                                style = MaterialTheme.typography.titleMedium,
                                color = AmakaColors.textPrimary
                            )
                        }
                        Text(
                            text = "Calories",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmakaColors.textTertiary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
            Text(
                text = completion.formattedStartTime,
                style = MaterialTheme.typography.labelSmall,
                color = AmakaColors.textTertiary
            )
        }
    }
}
