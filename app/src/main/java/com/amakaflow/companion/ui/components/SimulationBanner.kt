package com.amakaflow.companion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Yellow banner displayed during simulated workouts.
 * Shows simulation mode status and speed multiplier.
 */
@Composable
fun SimulationBanner(
    speed: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFFD600), // Bright yellow
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "SIMULATION MODE (${speed.toInt()}x)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
        }
    }
}

/**
 * Smaller simulation indicator badge for use in lists or cards.
 */
@Composable
fun SimulationBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFFD600).copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = Color(0xFFFFD600),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Simulated",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFFD600)
            )
        }
    }
}

/**
 * Badge indicating simulated health data.
 */
@Composable
fun SimulatedDataBadge(
    modifier: Modifier = Modifier
) {
    Text(
        text = "(simulated)",
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFFFD600),
        modifier = modifier
    )
}
