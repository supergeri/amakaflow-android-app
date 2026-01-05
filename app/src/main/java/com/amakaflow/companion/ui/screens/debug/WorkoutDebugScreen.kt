package com.amakaflow.companion.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@Composable
fun WorkoutDebugScreen(
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("No pending workouts") }
    var workouts by remember { mutableStateOf<List<String>>(emptyList()) }

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
                onClick = {
                    isLoading = true
                    status = "Fetching..."
                    // Simulate fetch - in real implementation this would call the API
                    // For now just reset after a moment
                    status = "No pending workouts"
                    isLoading = false
                },
                modifier = Modifier.weight(1f)
            )
            DebugActionButton(
                icon = Icons.Filled.Add,
                label = "Sample",
                color = AmakaColors.accentGreen,
                onClick = {
                    // Add a sample workout for testing
                    workouts = workouts + "Sample Workout ${workouts.size + 1}"
                    status = "${workouts.size} workout(s) loaded"
                },
                modifier = Modifier.weight(1f)
            )
            DebugActionButton(
                icon = Icons.Filled.ContentCopy,
                label = "Copy",
                color = Color(0xFF6B5B95), // Purple
                onClick = {
                    // Copy workout data to clipboard
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

        // Status bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AmakaColors.surface
        ) {
            Text(
                text = "Status: $status",
                style = MaterialTheme.typography.bodySmall,
                color = AmakaColors.textSecondary,
                modifier = Modifier.padding(horizontal = AmakaSpacing.md.dp, vertical = AmakaSpacing.sm.dp)
            )
        }

        // Content area
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AmakaSpacing.xl.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmakaColors.accentBlue)
            }
        } else if (workouts.isEmpty()) {
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
            // Workout list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AmakaSpacing.md.dp),
                verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
            ) {
                items(workouts) { workout ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AmakaColors.surface,
                        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                    ) {
                        Text(
                            text = workout,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmakaColors.textPrimary,
                            modifier = Modifier.padding(AmakaSpacing.md.dp)
                        )
                    }
                }
            }
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
