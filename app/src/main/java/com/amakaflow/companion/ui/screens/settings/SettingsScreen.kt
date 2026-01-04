package com.amakaflow.companion.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.amakaflow.companion.data.AppEnvironment
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEnvironmentDialog by remember { mutableStateOf(false) }
    var showUnpairDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
            .padding(horizontal = AmakaSpacing.md.dp),
        verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = AmakaColors.textPrimary
            )
            Spacer(modifier = Modifier.height(AmakaSpacing.lg.dp))
        }

        // Account section
        item {
            SectionHeader("Account")
        }
        item {
            if (uiState.isPaired) {
                SettingsItem(
                    icon = Icons.Filled.Person,
                    title = uiState.userEmail ?: "Connected",
                    subtitle = "Device paired",
                    onClick = {}
                )
            } else {
                SettingsItem(
                    icon = Icons.Filled.QrCodeScanner,
                    title = "Pair Device",
                    subtitle = "Scan QR code to connect",
                    onClick = { /* Navigate to pairing */ }
                )
            }
        }

        // Device section
        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
            SectionHeader("Device")
        }
        item {
            SettingsItem(
                icon = Icons.Filled.Watch,
                title = "Wearable",
                subtitle = "Configure smartwatch connection",
                onClick = {}
            )
        }

        // Developer section
        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
            SectionHeader("Developer")
        }
        item {
            SettingsItem(
                icon = Icons.Filled.Code,
                title = "Environment",
                subtitle = uiState.environment.displayName,
                onClick = { showEnvironmentDialog = true }
            )
        }
        item {
            SettingsItem(
                icon = Icons.Filled.BugReport,
                title = "Debug Logs",
                subtitle = "View app logs",
                onClick = {}
            )
        }

        // Disconnect section
        if (uiState.isPaired) {
            item {
                Spacer(modifier = Modifier.height(AmakaSpacing.lg.dp))
            }
            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Disconnect Device",
                    subtitle = "Unpair this device",
                    iconTint = AmakaColors.accentRed,
                    onClick = { showUnpairDialog = true }
                )
            }
        }

        // App info
        item {
            Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AmakaFlow Companion",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmakaColors.textTertiary
                )
                Text(
                    text = "Version ${uiState.appVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmakaColors.textTertiary
                )
            }
            Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
        }
    }

    // Environment selection dialog
    if (showEnvironmentDialog) {
        AlertDialog(
            onDismissRequest = { showEnvironmentDialog = false },
            title = { Text("Select Environment") },
            text = {
                Column {
                    AppEnvironment.entries.forEach { env ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(AmakaCornerRadius.sm.dp))
                                .clickable {
                                    viewModel.setEnvironment(env)
                                    showEnvironmentDialog = false
                                }
                                .padding(AmakaSpacing.md.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.environment == env,
                                onClick = {
                                    viewModel.setEnvironment(env)
                                    showEnvironmentDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                            Text(
                                text = env.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEnvironmentDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = AmakaColors.surface,
            titleContentColor = AmakaColors.textPrimary,
            textContentColor = AmakaColors.textPrimary
        )
    }

    // Unpair confirmation dialog
    if (showUnpairDialog) {
        AlertDialog(
            onDismissRequest = { showUnpairDialog = false },
            title = { Text("Disconnect Device?") },
            text = {
                Text("This will remove the connection to your account. You can pair again anytime.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.unpair()
                        showUnpairDialog = false
                    }
                ) {
                    Text(
                        text = "Disconnect",
                        color = AmakaColors.accentRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnpairDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = AmakaColors.surface,
            titleContentColor = AmakaColors.textPrimary,
            textContentColor = AmakaColors.textSecondary
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = AmakaColors.textSecondary,
        modifier = Modifier.padding(vertical = AmakaSpacing.sm.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color = AmakaColors.textSecondary,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AmakaColors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AmakaColors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AmakaColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
