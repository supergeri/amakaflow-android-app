package com.amakaflow.companion.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amakaflow.companion.simulation.SimulationSettings
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing
import kotlinx.coroutines.launch

/**
 * Debug settings screen for configuring workout simulation mode.
 * Access via 7-tap gesture on Settings icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(
    settings: SimulationSettings,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val isEnabled by settings.isEnabled.collectAsState(initial = false)
    val speed by settings.speed.collectAsState(initial = 10.0)
    val profileName by settings.profileName.collectAsState(initial = "casual")
    val generateHealth by settings.generateHealthData.collectAsState(initial = true)
    val restingHR by settings.restingHR.collectAsState(initial = 70)
    val maxHR by settings.maxHR.collectAsState(initial = 175)

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
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AmakaColors.textPrimary
                )
            }
            Text(
                text = "Debug Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AmakaColors.textPrimary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AmakaSpacing.md.dp),
            verticalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
        ) {
            // SIMULATION section
            item {
                Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                SectionHeader("WORKOUT SIMULATION")
            }

            // Enable toggle
            item {
                ToggleSettingItem(
                    icon = Icons.Filled.Bolt,
                    iconBackground = Color.Yellow.copy(alpha = 0.2f),
                    iconTint = Color(0xFFFFD600),
                    title = "Enable Simulation Mode",
                    subtitle = "Auto-advance through workouts at accelerated speed",
                    isEnabled = isEnabled,
                    onToggle = { scope.launch { settings.setEnabled(it) } }
                )
            }

            if (isEnabled) {
                // Speed slider
                item {
                    SpeedSliderItem(
                        speed = speed,
                        onSpeedChange = { scope.launch { settings.setSpeed(it) } }
                    )
                }

                // Behavior profile selector
                item {
                    BehaviorProfileSelector(
                        selectedProfile = profileName,
                        onProfileSelected = { scope.launch { settings.setProfile(it) } }
                    )
                }

                // Generate health data toggle
                item {
                    Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))
                    SectionHeader("HEALTH DATA SIMULATION")
                }

                item {
                    ToggleSettingItem(
                        icon = Icons.Filled.Favorite,
                        iconBackground = AmakaColors.accentRed.copy(alpha = 0.2f),
                        iconTint = AmakaColors.accentRed,
                        title = "Generate Fake Health Data",
                        subtitle = "Simulate heart rate, calories, and steps",
                        isEnabled = generateHealth,
                        onToggle = { scope.launch { settings.setGenerateHealthData(it) } }
                    )
                }

                if (generateHealth) {
                    // HR Profile settings
                    item {
                        HRProfileSettings(
                            restingHR = restingHR,
                            maxHR = maxHR,
                            onRestingHRChange = { scope.launch { settings.setRestingHR(it) } },
                            onMaxHRChange = { scope.launch { settings.setMaxHR(it) } }
                        )
                    }
                }
            }

            // Info text
            item {
                Spacer(modifier = Modifier.height(AmakaSpacing.lg.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = AmakaColors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(AmakaSpacing.sm.dp))
                    Text(
                        text = "Simulation mode is for testing only. Simulated workouts are marked with a yellow banner and flagged in API submissions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textTertiary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = AmakaColors.textTertiary,
        modifier = Modifier.padding(vertical = AmakaSpacing.sm.dp)
    )
}

@Composable
private fun ToggleSettingItem(
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = iconBackground,
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AmakaColors.textPrimary,
                    checkedTrackColor = AmakaColors.accentBlue,
                    uncheckedThumbColor = AmakaColors.textTertiary,
                    uncheckedTrackColor = AmakaColors.surface
                )
            )
        }
    }
}

@Composable
private fun SpeedSliderItem(
    speed: Double,
    onSpeedChange: (Double) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = AmakaColors.accentBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = AmakaColors.accentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simulation Speed",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmakaColors.textPrimary
                    )
                    Text(
                        text = "${speed.toInt()}x faster than real-time",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

            // Speed slider
            Slider(
                value = speed.toFloat(),
                onValueChange = { onSpeedChange(it.toDouble()) },
                valueRange = 1f..60f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = AmakaColors.accentBlue,
                    activeTrackColor = AmakaColors.accentBlue,
                    inactiveTrackColor = AmakaColors.borderLight
                )
            )

            // Speed presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(1.0, 10.0, 30.0, 60.0).forEach { preset ->
                    SpeedPresetChip(
                        label = "${preset.toInt()}x",
                        isSelected = speed == preset,
                        onClick = { onSpeedChange(preset) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(AmakaCornerRadius.lg.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) AmakaColors.accentBlue else AmakaColors.background,
        shape = RoundedCornerShape(AmakaCornerRadius.lg.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) AmakaColors.textPrimary else AmakaColors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun BehaviorProfileSelector(
    selectedProfile: String,
    onProfileSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = AmakaColors.accentPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = AmakaColors.accentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))
                Column {
                    Text(
                        text = "User Behavior",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmakaColors.textPrimary
                    )
                    Text(
                        text = "Simulates different workout patterns",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

            // Profile chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.sm.dp)
            ) {
                ProfileChip(
                    label = "Efficient",
                    description = "Quick, focused",
                    isSelected = selectedProfile == "efficient",
                    onClick = { onProfileSelected("efficient") },
                    modifier = Modifier.weight(1f)
                )
                ProfileChip(
                    label = "Casual",
                    description = "Normal pace",
                    isSelected = selectedProfile == "casual",
                    onClick = { onProfileSelected("casual") },
                    modifier = Modifier.weight(1f)
                )
                ProfileChip(
                    label = "Distracted",
                    description = "Extra pauses",
                    isSelected = selectedProfile == "distracted",
                    onClick = { onProfileSelected("distracted") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileChip(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(AmakaCornerRadius.md.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) AmakaColors.accentPurple else AmakaColors.background,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier.padding(AmakaSpacing.sm.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) AmakaColors.textPrimary else AmakaColors.textSecondary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) AmakaColors.textPrimary.copy(alpha = 0.7f) else AmakaColors.textTertiary
            )
        }
    }
}

@Composable
private fun HRProfileSettings(
    restingHR: Int,
    maxHR: Int,
    onRestingHRChange: (Int) -> Unit,
    onMaxHRChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmakaColors.surface,
        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AmakaSpacing.md.dp)
        ) {
            Text(
                text = "Heart Rate Profile",
                style = MaterialTheme.typography.titleMedium,
                color = AmakaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(AmakaSpacing.md.dp))

            // Resting HR
            Text(
                text = "Resting HR: $restingHR bpm",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
            Slider(
                value = restingHR.toFloat(),
                onValueChange = { onRestingHRChange(it.toInt()) },
                valueRange = 50f..90f,
                colors = SliderDefaults.colors(
                    thumbColor = AmakaColors.accentRed,
                    activeTrackColor = AmakaColors.accentRed,
                    inactiveTrackColor = AmakaColors.borderLight
                )
            )

            Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))

            // Max HR
            Text(
                text = "Max HR: $maxHR bpm",
                style = MaterialTheme.typography.bodyMedium,
                color = AmakaColors.textSecondary
            )
            Slider(
                value = maxHR.toFloat(),
                onValueChange = { onMaxHRChange(it.toInt()) },
                valueRange = 150f..200f,
                colors = SliderDefaults.colors(
                    thumbColor = AmakaColors.accentRed,
                    activeTrackColor = AmakaColors.accentRed,
                    inactiveTrackColor = AmakaColors.borderLight
                )
            )
        }
    }
}
