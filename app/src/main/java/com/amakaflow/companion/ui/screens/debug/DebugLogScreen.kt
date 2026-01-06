package com.amakaflow.companion.ui.screens.debug

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amakaflow.companion.debug.DebugLog
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

    // Auto-scroll to bottom when new entries are added
    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.animateScrollToItem(entries.size - 1)
        }
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
                text = "Debug Logs",
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
                contentPadding = PaddingValues(horizontal = AmakaSpacing.sm.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    LogEntryRow(entry = entry)
                }
                item {
                    Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: LogEntry) {
    val backgroundColor = when (entry.level) {
        LogLevel.ERROR -> AmakaColors.accentRed.copy(alpha = 0.15f)
        LogLevel.WARNING -> AmakaColors.accentOrange.copy(alpha = 0.15f)
        LogLevel.SUCCESS -> AmakaColors.accentGreen.copy(alpha = 0.15f)
        LogLevel.DEBUG -> AmakaColors.textTertiary.copy(alpha = 0.1f)
        LogLevel.INFO -> Color.Transparent
    }

    val textColor = when (entry.level) {
        LogLevel.ERROR -> AmakaColors.accentRed
        LogLevel.WARNING -> AmakaColors.accentOrange
        LogLevel.SUCCESS -> AmakaColors.accentGreen
        LogLevel.DEBUG -> AmakaColors.textTertiary
        LogLevel.INFO -> AmakaColors.textSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = entry.formatted(),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = textColor,
            maxLines = 10
        )
    }
}
