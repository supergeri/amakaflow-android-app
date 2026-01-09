package com.amakaflow.companion.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaCornerRadius
import com.amakaflow.companion.ui.theme.AmakaSpacing
import kotlinx.coroutines.delay

/**
 * AMA-287: Weight input view for logging set weights during reps exercises.
 * Mirrors iOS WeightInputView functionality.
 */
@Composable
fun WeightInputView(
    exerciseName: String,
    setNumber: Int,
    totalSets: Int,
    suggestedWeight: Double?,
    weightUnit: String,
    onLogSet: (weight: Double?, unit: String) -> Unit,
    onSkipWeight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var weight by remember(suggestedWeight) {
        mutableDoubleStateOf(suggestedWeight ?: 0.0)
    }
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Weight increment based on unit
    val baseIncrement = if (weightUnit == "kg") 2.5 else 5.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AmakaSpacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Exercise name and set info
        Text(
            text = exerciseName.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = AmakaColors.textSecondary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))

        Text(
            text = "Set $setNumber/$totalSets",
            style = MaterialTheme.typography.titleMedium,
            color = AmakaColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(AmakaSpacing.lg.dp))

        // Weight adjustment row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus button with long-press acceleration
            RepeatingButton(
                onClick = {
                    weight = maxOf(0.0, weight - baseIncrement)
                },
                onLongPressStep = { stepIndex ->
                    val increment = when {
                        stepIndex < 3 -> baseIncrement
                        stepIndex < 8 -> baseIncrement * 2
                        else -> baseIncrement * 5
                    }
                    weight = maxOf(0.0, weight - increment)
                },
                enabled = weight > 0,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Decrease weight",
                    modifier = Modifier.size(28.dp),
                    tint = if (weight > 0) AmakaColors.textPrimary else AmakaColors.textTertiary
                )
            }

            Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))

            // Weight display (tappable for direct entry)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { newValue ->
                            // Only allow numbers and decimal point
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                editText = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                weight = editText.toDoubleOrNull() ?: 0.0
                                isEditing = false
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .width(150.dp)
                            .padding(vertical = AmakaSpacing.sm.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmakaColors.accentGreen,
                            unfocusedBorderColor = AmakaColors.surface
                        )
                    )
                } else {
                    Surface(
                        onClick = {
                            editText = if (weight > 0) formatWeight(weight) else ""
                            isEditing = true
                        },
                        color = Color.Transparent,
                        shape = RoundedCornerShape(AmakaCornerRadius.md.dp)
                    ) {
                        Text(
                            text = formatWeight(weight),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (weight > 0) AmakaColors.accentBlue else AmakaColors.textTertiary,
                            modifier = Modifier.padding(vertical = AmakaSpacing.sm.dp)
                        )
                    }
                }

                Text(
                    text = weightUnit,
                    style = MaterialTheme.typography.titleMedium,
                    color = AmakaColors.textSecondary
                )

                // Pre-fill hint
                if (suggestedWeight != null && suggestedWeight > 0) {
                    Spacer(modifier = Modifier.height(AmakaSpacing.xs.dp))
                    Text(
                        text = "(last: ${formatWeight(suggestedWeight)} $weightUnit)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmakaColors.textTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.width(AmakaSpacing.md.dp))

            // Plus button with long-press acceleration
            RepeatingButton(
                onClick = {
                    weight += baseIncrement
                },
                onLongPressStep = { stepIndex ->
                    val increment = when {
                        stepIndex < 3 -> baseIncrement
                        stepIndex < 8 -> baseIncrement * 2
                        else -> baseIncrement * 5
                    }
                    weight += increment
                },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Increase weight",
                    modifier = Modifier.size(28.dp),
                    tint = AmakaColors.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(AmakaSpacing.xl.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)
        ) {
            // Skip button
            OutlinedButton(
                onClick = onSkipWeight,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(AmakaCornerRadius.lg.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AmakaColors.textSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AmakaSpacing.xs.dp))
                Text(
                    text = "SKIP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Log Set button (primary)
            Button(
                onClick = {
                    val logWeight = if (weight > 0) weight else null
                    onLogSet(logWeight, weightUnit)
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(56.dp),
                shape = RoundedCornerShape(AmakaCornerRadius.lg.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmakaColors.accentGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AmakaSpacing.xs.dp))
                Text(
                    text = "LOG SET",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Button that repeats action while pressed with acceleration.
 */
@Composable
private fun RepeatingButton(
    onClick: () -> Unit,
    onLongPressStep: (stepIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick()
            delay(400)  // Initial delay before repeating
            var stepIndex = 0
            while (true) {
                onLongPressStep(stepIndex)
                stepIndex++
                // Speed up after more steps
                val delayMs = when {
                    stepIndex < 3 -> 200L
                    stepIndex < 8 -> 100L
                    else -> 50L
                }
                delay(delayMs)
            }
        }
    }

    Surface(
        onClick = { /* handled by interaction source */ },
        modifier = modifier,
        shape = CircleShape,
        color = AmakaColors.surface,
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

private fun formatWeight(weight: Double): String {
    return if (weight == 0.0) {
        "0"
    } else if (weight % 1.0 == 0.0) {
        weight.toInt().toString()
    } else {
        String.format("%.1f", weight)
    }
}
