package com.amakaflow.companion.ui.screens.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amakaflow.companion.data.model.Workout
import com.amakaflow.companion.data.model.WorkoutHelpers
import com.amakaflow.companion.data.model.WorkoutInterval
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

/**
 * Represents a flattened workout step for display
 */
data class FlattenedStep(
    val name: String,
    val type: String,
    val rest: String = "manual",
    val duration: Int? = null,
    val reps: Int? = null,
    val sets: Int? = null
)

/**
 * Flatten workout intervals into a linear list of steps
 */
fun flattenIntervals(intervals: List<WorkoutInterval>): List<FlattenedStep> {
    val steps = mutableListOf<FlattenedStep>()

    fun processInterval(interval: WorkoutInterval) {
        when (interval) {
            is WorkoutInterval.Warmup -> {
                steps.add(FlattenedStep(
                    name = "Warm Up",
                    type = "timed",
                    duration = interval.seconds
                ))
            }
            is WorkoutInterval.Cooldown -> {
                steps.add(FlattenedStep(
                    name = "Cool Down",
                    type = "timed",
                    duration = interval.seconds
                ))
            }
            is WorkoutInterval.Time -> {
                steps.add(FlattenedStep(
                    name = interval.target ?: "Work",
                    type = "timed",
                    duration = interval.seconds
                ))
            }
            is WorkoutInterval.Distance -> {
                steps.add(FlattenedStep(
                    name = interval.target ?: "Distance",
                    type = "distance"
                ))
            }
            is WorkoutInterval.Reps -> {
                steps.add(FlattenedStep(
                    name = interval.name,
                    type = "reps",
                    reps = interval.reps,
                    sets = interval.sets
                ))
            }
            is WorkoutInterval.Repeat -> {
                // Expand repeat intervals
                repeat(interval.reps) {
                    interval.intervals.forEach { nestedInterval ->
                        processInterval(nestedInterval)
                    }
                }
            }
        }
    }

    intervals.forEach { processInterval(it) }
    return steps
}

@Composable
fun WorkoutDebugScreen(
    onDismiss: () -> Unit,
    viewModel: WorkoutDebugViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AmakaSpacing.md.dp, vertical = AmakaSpacing.md.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(60.dp)) // Balance for centering

            Text(
                text = "Workout Debug",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AmakaColors.textPrimary
            )

            TextButton(onClick = onDismiss) {
                Text(
                    text = "Done",
                    color = AmakaColors.accentBlue,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Action buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AmakaSpacing.md.dp),
            horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
        ) {
            DebugActionButton(
                icon = Icons.Filled.Download,
                label = "Fetch",
                color = AmakaColors.accentBlue,
                onClick = { viewModel.fetchWorkouts() },
                modifier = Modifier.weight(1f)
            )
            DebugActionButton(
                icon = Icons.Filled.Add,
                label = "Sample",
                color = AmakaColors.accentGreen,
                onClick = { viewModel.addSampleWorkout() },
                modifier = Modifier.weight(1f)
            )
            DebugActionButton(
                icon = Icons.Filled.ContentCopy,
                label = "Copy",
                color = Color(0xFF6B5B95), // Purple
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Workout Debug", viewModel.generateDebugText())
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

        // Status bar with API info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AmakaColors.surface
        ) {
            Column(
                modifier = Modifier.padding(horizontal = AmakaSpacing.md.dp, vertical = AmakaSpacing.sm.dp)
            ) {
                Text(
                    text = "Status: ${uiState.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textSecondary
                )
                Text(
                    text = "API: ${uiState.apiUrl}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = AmakaColors.accentBlue
                )
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF6B6B)
                    )
                }
            }
        }

        // Content area
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AmakaSpacing.xl.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmakaColors.accentBlue)
            }
        } else if (uiState.workouts.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AmakaSpacing.lg.dp)
            ) {
                Text(
                    text = "No incoming workouts loaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmakaColors.textSecondary
                )
                Text(
                    text = "Tap 'Fetch' to check for pending workouts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmakaColors.textSecondary
                )
            }
        } else {
            // Workout list with details
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AmakaSpacing.md.dp),
                verticalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)
            ) {
                items(uiState.workouts) { workout ->
                    WorkoutDebugCard(workout = workout)
                }
            }
        }
    }
}

@Composable
private fun WorkoutDebugCard(workout: Workout) {
    val flattenedSteps = remember(workout) { flattenIntervals(workout.intervals) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            // Header with name and sport badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmakaColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = AmakaColors.accentBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = workout.sport.name.lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = AmakaColors.accentBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ID
            Text(
                text = "ID: ${workout.id}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = AmakaColors.textTertiary
            )

            // Duration
            Text(
                text = "Duration: ${workout.duration}s (${workout.formattedDuration})",
                style = MaterialTheme.typography.bodySmall,
                color = AmakaColors.textSecondary
            )

            // Raw Intervals section (condensed)
            if (workout.intervals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                HorizontalDivider(color = AmakaColors.borderLight)
                Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

                Text(
                    text = "RAW INTERVALS (${workout.intervals.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmakaColors.accentGreen
                )

                Spacer(modifier = Modifier.height(4.dp))

                workout.intervals.forEachIndexed { index, interval ->
                    RawIntervalRow(index = index, interval = interval)
                }
            }

            // Flattened Steps section
            if (flattenedSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                HorizontalDivider(color = AmakaColors.borderLight)
                Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

                Text(
                    text = "FLATTENED STEPS (${flattenedSteps.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700) // Gold
                )

                Spacer(modifier = Modifier.height(8.dp))

                flattenedSteps.forEachIndexed { index, step ->
                    FlattenedStepRow(index = index, step = step)
                }
            }
        }
    }
}

@Composable
private fun RawIntervalRow(
    index: Int,
    interval: WorkoutInterval
) {
    val (label, color, subInfo) = when (interval) {
        is WorkoutInterval.Warmup -> Triple(
            "WARMUP",
            Color(0xFFFFA500),
            "seconds=${interval.seconds}, target=${interval.target ?: "nil"}"
        )
        is WorkoutInterval.Cooldown -> Triple(
            "COOLDOWN",
            Color(0xFF00CED1),
            "seconds=${interval.seconds}, target=${interval.target ?: "nil"}"
        )
        is WorkoutInterval.Time -> Triple(
            "TIME",
            AmakaColors.accentGreen,
            "seconds=${interval.seconds}, target=${interval.target ?: "nil"}"
        )
        is WorkoutInterval.Distance -> Triple(
            "DISTANCE",
            AmakaColors.accentBlue,
            "meters=${interval.meters}, target=${interval.target ?: "nil"}"
        )
        is WorkoutInterval.Reps -> Triple(
            "REPS",
            Color(0xFFFF69B4),
            "${interval.sets ?: 1}x${interval.reps} ${interval.name}"
        )
        is WorkoutInterval.Repeat -> Triple(
            "REPEAT x${interval.reps}",
            Color(0xFFFFD700),
            "contains ${interval.intervals.size} sub-intervals"
        )
    }

    Column(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "[$index] $label",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = "  $subInfo",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            ),
            color = AmakaColors.textTertiary
        )
    }
}

@Composable
private fun FlattenedStepRow(
    index: Int,
    step: FlattenedStep
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index
        Text(
            text = "[$index]",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            color = AmakaColors.textTertiary,
            modifier = Modifier.width(32.dp)
        )

        // Yellow circle indicator
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD700)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.type.first().uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Step info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = AmakaColors.textPrimary
            )
            Text(
                text = "type=${step.type}  rest=${step.rest}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = AmakaColors.textTertiary
            )
        }
    }
}

@Composable
private fun DebugActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(AmakaCornerRadius.md.dp)),
        color = color,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AmakaColors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AmakaColors.textPrimary
            )
        }
    }
}
