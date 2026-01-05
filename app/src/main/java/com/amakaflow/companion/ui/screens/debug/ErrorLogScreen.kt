package com.amakaflow.companion.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ErrorLogEntry(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime
)

@Composable
fun ErrorLogScreen(
    onDismiss: () -> Unit
) {
    // Sample error entries for demo - in real app this would come from a ViewModel
    var errorEntries by remember {
        mutableStateOf(
            listOf(
                ErrorLogEntry(
                    id = "1",
                    type = "WATCH_ERROR",
                    title = "Watch app not installed",
                    description = "Please install the watch app first",
                    timestamp = LocalDateTime.now().minusMinutes(5)
                ),
                ErrorLogEntry(
                    id = "2",
                    type = "WATCH_ERROR",
                    title = "Watch app not installed",
                    description = "Please install the watch app first",
                    timestamp = LocalDateTime.now().minusMinutes(10)
                ),
                ErrorLogEntry(
                    id = "3",
                    type = "NETWORK_ERROR",
                    title = "Connection failed",
                    description = "Unable to reach server",
                    timestamp = LocalDateTime.now().minusMinutes(15)
                )
            )
        )
    }

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
                text = "Error Log",
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
            ) {
                // Copy All button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AmakaCornerRadius.md.dp)),
                    color = AmakaColors.accentBlue,
                    shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
                    onClick = {
                        // Copy all entries to clipboard
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            tint = AmakaColors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Copy All",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmakaColors.textPrimary
                        )
                    }
                }

                // Clear button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(AmakaCornerRadius.md.dp)),
                    color = AmakaColors.accentRed,
                    shape = RoundedCornerShape(AmakaCornerRadius.md.dp),
                    onClick = {
                        errorEntries = emptyList()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = AmakaColors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelMedium,
                            color = AmakaColors.textPrimary
                        )
                    }
                }
            }

            // Entry count
            Text(
                text = "${errorEntries.size} entries",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

        // Error entries list
        if (errorEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AmakaSpacing.xl.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No errors logged",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AmakaColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AmakaSpacing.md.dp),
                verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
            ) {
                items(errorEntries) { entry ->
                    ErrorLogItem(entry = entry)
                }
                item {
                    Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
                }
            }
        }
    }
}

@Composable
private fun ErrorLogItem(entry: ErrorLogEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Error type badge
                    Surface(
                        color = getErrorTypeColor(entry.type),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmakaColors.textPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AmakaColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textSecondary
                )
            }

            Text(
                text = entry.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                style = MaterialTheme.typography.labelSmall,
                color = AmakaColors.textTertiary
            )
        }
    }
}

private fun getErrorTypeColor(type: String): Color {
    return when {
        type.contains("WATCH") -> AmakaColors.accentRed
        type.contains("NETWORK") -> AmakaColors.accentOrange
        type.contains("AUTH") -> Color(0xFF9C27B0) // Purple
        type.contains("SYNC") -> AmakaColors.accentBlue
        else -> AmakaColors.accentRed
    }
}
