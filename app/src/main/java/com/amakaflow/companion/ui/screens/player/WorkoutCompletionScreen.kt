package com.amakaflow.companion.ui.screens.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@Composable
fun WorkoutCompletionScreen(
    workoutName: String,
    durationSeconds: Int,
    onDone: () -> Unit,
    onRunAgain: () -> Unit
) {
    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
            .padding(AmakaSpacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Success icon with pulse
        Box(contentAlignment = Alignment.Center) {
            // Pulse ring
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .scale(pulseScale)
                    .background(
                        AmakaColors.accentGreen.copy(alpha = pulseAlpha),
                        CircleShape
                    )
            )

            // Outer ring
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(
                        AmakaColors.accentGreen.copy(alpha = 0.2f),
                        CircleShape
                    )
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(AmakaColors.accentGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))

        // Title
        Text(
            text = "Workout Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AmakaColors.textPrimary
        )

        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

        // Workout name
        Text(
            text = workoutName,
            style = MaterialTheme.typography.bodyLarge,
            color = AmakaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
        ) {
            // Duration
            StatCard(
                icon = Icons.Filled.Schedule,
                iconColor = AmakaColors.accentBlue,
                label = "Duration",
                value = formatDuration(durationSeconds),
                modifier = Modifier.weight(1f)
            )

            // Calories (placeholder)
            StatCard(
                icon = Icons.Filled.LocalFireDepartment,
                iconColor = AmakaColors.textTertiary,
                label = "Calories",
                value = "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
        ) {
            // Avg HR (placeholder)
            StatCard(
                icon = Icons.Filled.Favorite,
                iconColor = AmakaColors.textTertiary,
                label = "Avg HR",
                value = "--",
                modifier = Modifier.weight(1f)
            )

            // Max HR (placeholder)
            StatCard(
                icon = Icons.Filled.FavoriteBorder,
                iconColor = AmakaColors.textTertiary,
                label = "Max HR",
                value = "--",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))

        // No heart rate data message
        Surface(
            color = AmakaColors.surface,
            shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(AmakaSpacing.lg.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Watch,
                    contentDescription = null,
                    tint = AmakaColors.textTertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
                Text(
                    text = "No heart rate data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmakaColors.textSecondary
                )
                Text(
                    text = "Connect a wearable to track heart rate during workouts",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Button(
            onClick = onRunAgain,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmakaColors.surface
            ),
            shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Replay,
                contentDescription = null,
                tint = AmakaColors.textPrimary
            )
            Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
            Text(
                text = "Run Again",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AmakaColors.textPrimary
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmakaColors.accentBlue
            ),
            shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.lg.dp))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.xs.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AmakaColors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AmakaColors.textPrimary
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
