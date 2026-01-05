package com.amakaflow.companion.ui.screens.completion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amakaflow.companion.data.model.CompletionSource
import com.amakaflow.companion.data.model.WorkoutCompletionDetail
import com.amakaflow.companion.data.model.WorkoutInterval
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionDetailScreen(
    completionId: String,
    onNavigateBack: () -> Unit,
    viewModel: CompletionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AmakaSpacing.sm.dp, vertical = AmakaSpacing.sm.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AmakaColors.textPrimary
                )
            }
            Text(
                text = "Workout Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error loading details",
                            style = MaterialTheme.typography.titleMedium,
                            color = AmakaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textSecondary
                        )
                    }
                }
            }
            uiState.completion != null -> {
                CompletionDetailContent(completion = uiState.completion!!)
            }
        }
    }
}

@Composable
private fun CompletionDetailContent(completion: WorkoutCompletionDetail) {
    val localDateTime = completion.startedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val dateString = "${months[localDateTime.monthNumber - 1]} ${localDateTime.dayOfMonth}, ${localDateTime.year}"

    // Format start time
    val startHour = localDateTime.hour
    val startMinute = localDateTime.minute
    val startAmPm = if (startHour >= 12) "PM" else "AM"
    val startHour12 = if (startHour == 0) 12 else if (startHour > 12) startHour - 12 else startHour
    val startTimeString = String.format("%d:%02d %s", startHour12, startMinute, startAmPm)

    // Format end time
    val endInstant = completion.resolvedEndedAt
    val endLocalDateTime = endInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    val endHour = endLocalDateTime.hour
    val endMinute = endLocalDateTime.minute
    val endAmPm = if (endHour >= 12) "PM" else "AM"
    val endHour12 = if (endHour == 0) 12 else if (endHour > 12) endHour - 12 else endHour
    val endTimeString = String.format("%d:%02d %s", endHour12, endMinute, endAmPm)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AmakaSpacing.md.dp),
        verticalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)
    ) {
        // Header section (like iOS)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AmakaColors.surface,
                shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AmakaSpacing.md.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Workout name
                    Text(
                        text = completion.workoutName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AmakaColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))

                    // Date
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmakaColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

                    // Prominent duration display
                    Text(
                        text = completion.formattedDuration,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmakaColors.textPrimary
                    )
                    Text(
                        text = "Duration",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmakaColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

                    // Time range (start → end)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = AmakaColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = startTimeString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textSecondary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = AmakaColors.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = endTimeString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textSecondary
                        )
                    }
                }
            }
        }

        // Activity metrics card (if available)
        if (completion.hasSummaryMetrics || completion.distanceMeters != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AmakaColors.surface,
                    shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                ) {
                    Column(modifier = Modifier.padding(AmakaSpacing.md.dp)) {
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AmakaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            completion.activeCalories?.let { cal ->
                                StatItem(
                                    icon = Icons.Filled.LocalFireDepartment,
                                    value = "$cal",
                                    label = "Calories",
                                    iconTint = AmakaColors.accentOrange
                                )
                            }
                            completion.steps?.let { steps ->
                                StatItem(
                                    icon = Icons.Filled.DirectionsWalk,
                                    value = if (steps >= 1000) String.format("%.1fk", steps / 1000.0) else "$steps",
                                    label = "Steps",
                                    iconTint = AmakaColors.accentGreen
                                )
                            }
                            completion.distanceMeters?.let { meters ->
                                val distanceStr = if (meters >= 1000) {
                                    String.format("%.2f km", meters / 1000.0)
                                } else {
                                    "$meters m"
                                }
                                StatItem(
                                    icon = Icons.Filled.Straighten,
                                    value = distanceStr,
                                    label = "Distance",
                                    iconTint = AmakaColors.accentBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // Heart rate details (if available)
        if (completion.hasHeartRateData) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AmakaColors.surface,
                    shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                ) {
                    Column(modifier = Modifier.padding(AmakaSpacing.md.dp)) {
                        Text(
                            text = "Heart Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AmakaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            completion.minHeartRate?.let {
                                MiniStatItem(value = "$it", label = "Min")
                            }
                            completion.avgHeartRate?.let {
                                MiniStatItem(value = "$it", label = "Avg")
                            }
                            completion.maxHeartRate?.let {
                                MiniStatItem(value = "$it", label = "Max")
                            }
                        }
                    }
                }
            }
        }

        // Workout Steps section (like iOS - AMA-224)
        val workoutSteps = completion.workoutStructure
        if (!workoutSteps.isNullOrEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AmakaColors.surface,
                    shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                ) {
                    Column(modifier = Modifier.padding(AmakaSpacing.md.dp)) {
                        Text(
                            text = "Workout Steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AmakaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

                        // Flatten and display steps
                        val flattenedSteps = flattenWorkoutIntervals(workoutSteps)
                        flattenedSteps.forEachIndexed { index, step ->
                            WorkoutStepRow(
                                stepNumber = index + 1,
                                step = step
                            )
                            if (index < flattenedSteps.size - 1) {
                                Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
                            }
                        }
                    }
                }
            }
        }

        // Details section (Source device)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AmakaColors.surface,
                shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
            ) {
                Column(modifier = Modifier.padding(AmakaSpacing.md.dp)) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AmakaColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

                    // Source device row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (completion.source) {
                                CompletionSource.PHONE -> Icons.Filled.Smartphone
                                else -> Icons.Filled.Watch
                            },
                            contentDescription = null,
                            tint = AmakaColors.textSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))
                        Text(
                            text = "Source",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = completion.deviceInfo?.displayName ?: completion.source.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textPrimary
                        )
                    }

                    // Strava sync status row
                    if (completion.isSyncedToStrava) {
                        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = AmakaColors.accentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))
                            Text(
                                text = "Strava",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmakaColors.textSecondary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Synced",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmakaColors.accentGreen
                            )
                        }
                    }
                }
            }
        }

        // Strava sync button (always show - as placeholder for future functionality)
        item {
            Button(
                onClick = { /* TODO: Implement Strava sync */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmakaColors.accentOrange
                ),
                shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
            ) {
                Icon(
                    imageVector = if (completion.isSyncedToStrava) Icons.Filled.Link else Icons.Filled.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                Text(
                    text = if (completion.isSyncedToStrava) "View on Strava" else "Sync to Strava",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
        }
    }
}

/**
 * Flattens nested workout intervals into a simple list for display
 */
private fun flattenWorkoutIntervals(intervals: List<WorkoutInterval>): List<WorkoutInterval> {
    val result = mutableListOf<WorkoutInterval>()
    for (interval in intervals) {
        when (interval) {
            is WorkoutInterval.Repeat -> {
                // Expand repeat blocks
                repeat(interval.reps) {
                    result.addAll(flattenWorkoutIntervals(interval.intervals))
                }
            }
            else -> result.add(interval)
        }
    }
    return result
}

/**
 * Display a single workout step row
 */
@Composable
private fun WorkoutStepRow(stepNumber: Int, step: WorkoutInterval) {
    val (name, detail, target, iconColor) = when (step) {
        is WorkoutInterval.Warmup -> {
            Tuple4("Warm Up", formatTime(step.seconds), step.target, AmakaColors.accentOrange)
        }
        is WorkoutInterval.Cooldown -> {
            Tuple4("Cool Down", formatTime(step.seconds), step.target, AmakaColors.accentBlue)
        }
        is WorkoutInterval.Time -> {
            Tuple4("Timed Interval", formatTime(step.seconds), step.target, AmakaColors.accentGreen)
        }
        is WorkoutInterval.Reps -> {
            var detailStr = "${step.reps} reps"
            if (step.sets != null && step.sets > 1) {
                detailStr = "${step.sets} × ${step.reps} reps"
            }
            if (step.load != null && step.load.isNotEmpty()) {
                detailStr += " @ ${step.load}"
            }
            Tuple4(step.name, detailStr, null, AmakaColors.accentPurple)
        }
        is WorkoutInterval.Distance -> {
            val distStr = if (step.meters >= 1000) {
                String.format("%.1f km", step.meters / 1000.0)
            } else {
                "${step.meters}m"
            }
            Tuple4("Distance", distStr, step.target, AmakaColors.accentGreen)
        }
        is WorkoutInterval.Repeat -> {
            // This shouldn't happen after flattening, but handle it anyway
            Tuple4("Repeat ${step.reps}x", "${step.intervals.size} exercises", null, AmakaColors.textSecondary)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))

        // Name and detail
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AmakaColors.textPrimary
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = AmakaColors.textSecondary
            )
            if (target != null && target.isNotEmpty()) {
                Text(
                    text = target,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmakaColors.textTertiary
                )
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatTime(seconds: Int): String {
    return if (seconds >= 60) {
        val minutes = seconds / 60
        val secs = seconds % 60
        if (secs > 0) {
            "${minutes}m ${secs}s"
        } else {
            "$minutes min"
        }
    } else {
        "${seconds}s"
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AmakaColors.textPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AmakaColors.textTertiary
        )
    }
}

@Composable
private fun MiniStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AmakaColors.textPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AmakaColors.textTertiary
        )
    }
}

private val CompletionSource.displayName: String
    get() = when (this) {
        CompletionSource.APPLE_WATCH -> "Apple Watch"
        CompletionSource.GARMIN -> "Garmin"
        CompletionSource.MANUAL -> "Manual"
        CompletionSource.PHONE -> "Phone"
        CompletionSource.WEAR_OS -> "Wear OS"
    }
