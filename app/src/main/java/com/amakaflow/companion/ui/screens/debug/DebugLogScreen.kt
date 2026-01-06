package com.amakaflow.companion.ui.screens.debug

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amakaflow.companion.debug.DebugLog
import com.amakaflow.companion.debug.ErrorType
import com.amakaflow.companion.debug.LogEntry
import com.amakaflow.companion.debug.LogLevel
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@Composable
fun DebugLogScreen(
    onDismiss: () -> Unit
) {
    val entries by DebugLog.entries.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

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
                text = "Debug Log",
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
                        clipboardManager.setText(AnnotatedString(DebugLog.copyableText()))
                        Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
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
                    onClick = { DebugLog.clear() }
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
                text = "${entries.size} entries",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

        // Log entries list
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AmakaSpacing.xl.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logs yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AmakaColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AmakaSpacing.md.dp),
                verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    LogEntryCard(entry = entry)
                }
                item {
                    Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(entry: LogEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier.padding(AmakaSpacing.md.dp)
        ) {
            // Top row: Error type badge + timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Error type badge
                ErrorTypeBadge(
                    errorType = entry.errorType,
                    level = entry.level
                )

                // Timestamp
                Text(
                    text = entry.formattedDateTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AmakaColors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AmakaColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Details (if present)
            entry.details?.let { details ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // API-specific details (if present)
            if (entry.endpoint != null || entry.method != null || entry.statusCode != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ApiDetailsSection(entry = entry)
            }
        }
    }
}

@Composable
private fun ErrorTypeBadge(
    errorType: ErrorType,
    level: LogLevel
) {
    val (backgroundColor, textColor) = when (errorType) {
        ErrorType.API_ERROR -> AmakaColors.accentRed to Color.White
        ErrorType.WATCH_ERROR -> AmakaColors.accentOrange to Color.White
        ErrorType.NETWORK_ERROR -> AmakaColors.accentOrange to Color.White
        ErrorType.AUTH_ERROR -> Color(0xFF9C27B0) to Color.White // Purple
        ErrorType.SYNC_ERROR -> AmakaColors.accentBlue to Color.White
        ErrorType.APP_ERROR -> AmakaColors.accentRed to Color.White
        ErrorType.GENERAL -> when (level) {
            LogLevel.SUCCESS -> AmakaColors.accentGreen to Color.White
            LogLevel.WARNING -> AmakaColors.accentOrange to Color.White
            LogLevel.ERROR -> AmakaColors.accentRed to Color.White
            LogLevel.DEBUG -> AmakaColors.textTertiary to AmakaColors.textPrimary
            LogLevel.INFO -> AmakaColors.accentBlue.copy(alpha = 0.3f) to AmakaColors.textPrimary
        }
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = errorType.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ApiDetailsSection(entry: LogEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AmakaColors.background.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        entry.endpoint?.let { endpoint ->
            ApiDetailRow(label = "Endpoint", value = endpoint)
        }
        entry.method?.let { method ->
            ApiDetailRow(label = "Method", value = method)
        }
        entry.statusCode?.let { statusCode ->
            ApiDetailRow(label = "Status", value = statusCode.toString())
        }
        entry.response?.let { response ->
            ApiDetailRow(label = "Response", value = response)
        }
    }
}

@Composable
private fun ApiDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = AmakaColors.textTertiary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = AmakaColors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
