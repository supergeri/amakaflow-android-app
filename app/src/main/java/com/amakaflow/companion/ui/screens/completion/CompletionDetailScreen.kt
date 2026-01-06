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
import com.amakaflow.companion.data.model.WorkoutIntervalSubmission
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
    onRunAgain: ((String) -> Unit)? = null,
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
                CompletionDetailContent(
                    completion = uiState.completion!!,
                    onRunAgain = onRunAgain
                )
            }
        }
    }
}

@Composable
private fun CompletionDetailContent(
    completion: WorkoutCompletionDetail,
    onRunAgain: ((String) -> Unit)? = null
) {
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

        // Workout Breakdown section (hierarchical like web app - AMA-264)
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
                            text = "Workout Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AmakaColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

                        // Display hierarchical workout structure (like web app)
                        workoutSteps.forEachIndexed { index, interval ->
                            WorkoutIntervalRow(
                                stepNumber = index + 1,
                                interval = interval
                            )
                            if (index < workoutSteps.size - 1) {
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

        // Run Again button (only if workout has a workoutId)
        if (completion.workoutId != null && onRunAgain != null) {
            item {
                Button(
                    onClick = { onRunAgain(completion.workoutId!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmakaColors.accentGreen
                    ),
                    shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                    Text(
                        text = "Run Again",
                        fontWeight = FontWeight.SemiBold
                    )
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
 * Display a workout interval row with hierarchical support.
 * Handles repeat blocks with nested content, showing them like the web app.
 */
@Composable
private fun WorkoutIntervalRow(stepNumber: Int, interval: WorkoutIntervalSubmission) {
    when (interval.type.lowercase()) {
        "repeat" -> {
            // Display repeat block with nested content (like web app)
            RepeatBlockRow(stepNumber = stepNumber, repeat = interval)
        }
        else -> {
            // Display single interval
            SingleIntervalRow(stepNumber = stepNumber, interval = interval)
        }
    }
}

/**
 * Display a repeat block with its nested intervals (like web app).
 * Shows: "Repeat [n] - [sets] sets" with nested exercise and rest steps indented.
 */
@Composable
private fun RepeatBlockRow(stepNumber: Int, repeat: WorkoutIntervalSubmission) {
    val sets = repeat.reps ?: 1
    val setsText = if (sets == 1) "1 set" else "$sets sets"

    Column(modifier = Modifier.fillMaxWidth()) {
        // Repeat header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AmakaColors.accentPurple),
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

            // Repeat label with sets count
            Text(
                text = "Repeat $stepNumber",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AmakaColors.textPrimary
            )

            Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))

            Text(
                text = "- $setsText",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
        }

        // Nested intervals (indented)
        if (!repeat.intervals.isNullOrEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp, top = AmakaSpacing.xs.dp)
            ) {
                repeat.intervals.forEach { nested ->
                    NestedIntervalRow(interval = nested)
                    Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))
                }
            }
        }
    }
}

/**
 * Display a nested interval within a repeat block (indented, no number badge).
 */
@Composable
private fun NestedIntervalRow(interval: WorkoutIntervalSubmission) {
    val (name, detail, iconColor) = getIntervalDisplayInfo(interval)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small dot indicator instead of number
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(iconColor)
        )

        Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))

        // Name
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = AmakaColors.textPrimary
        )

        if (detail.isNotEmpty()) {
            Spacer(modifier = Modifier.width(AmakaSpacing.xs.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = AmakaColors.textSecondary
            )
        }
    }
}

/**
 * Display a single interval row (not inside a repeat block).
 */
@Composable
private fun SingleIntervalRow(stepNumber: Int, interval: WorkoutIntervalSubmission) {
    val (name, detail, iconColor) = getIntervalDisplayInfo(interval)

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
            if (detail.isNotEmpty()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textSecondary
                )
            }
        }
    }
}

/**
 * Get display info for an interval type.
 * Returns Triple of (name, detail, iconColor)
 */
private fun getIntervalDisplayInfo(interval: WorkoutIntervalSubmission): Triple<String, String, Color> {
    return when (interval.type.lowercase()) {
        "warmup" -> {
            val name = interval.target?.takeIf { it.isNotEmpty() } ?: "Warm Up"
            Triple(name, interval.seconds?.let { formatTime(it) } ?: "", AmakaColors.accentOrange)
        }
        "cooldown" -> {
            val name = interval.target?.takeIf { it.isNotEmpty() } ?: "Cool Down"
            Triple(name, interval.seconds?.let { formatTime(it) } ?: "", AmakaColors.accentBlue)
        }
        "time" -> {
            val name = interval.target?.takeIf { it.isNotEmpty() } ?: "Timed Interval"
            Triple(name, interval.seconds?.let { formatTime(it) } ?: "", AmakaColors.accentGreen)
        }
        "reps" -> {
            val name = interval.name ?: "Exercise"
            var detail = "${interval.reps ?: 0} reps"
            if (interval.sets != null && interval.sets > 1) {
                detail = "${interval.sets} × ${interval.reps ?: 0} reps"
            }
            Triple(name, detail, AmakaColors.accentPurple)
        }
        "distance" -> {
            val name = interval.target?.takeIf { it.isNotEmpty() } ?: "Distance"
            val meters = interval.seconds ?: 0
            val distStr = if (meters >= 1000) {
                String.format("%.1f km", meters / 1000.0)
            } else {
                "${meters}m"
            }
            Triple(name, distStr, AmakaColors.accentGreen)
        }
        "rest" -> {
            val detail = interval.seconds?.let { formatTime(it) } ?: "Tap when ready"
            Triple("Rest", detail, AmakaColors.textTertiary)
        }
        else -> {
            Triple(interval.type.replaceFirstChar { it.uppercase() }, "", AmakaColors.textSecondary)
        }
    }
}

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
