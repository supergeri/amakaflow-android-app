package com.amakaflow.companion.ui.screens.pairing

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amakaflow.companion.BuildConfig
import com.amakaflow.companion.data.TestConfig
import com.amakaflow.companion.ui.components.QRCodeScanner
import com.amakaflow.companion.ui.theme.AmakaColors
import com.amakaflow.companion.ui.theme.AmakaSpacing

@Composable
fun PairingScreen(
    onPairingComplete: () -> Unit,
    testConfig: TestConfig,
    viewModel: PairingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Test mode dialog state (only in debug builds)
    var showTestModeDialog by remember { mutableStateOf(false) }
    var testAuthSecret by remember { mutableStateOf("") }
    var testUserId by remember { mutableStateOf("") }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            viewModel.setMode(PairingMode.MANUAL_CODE)
        }
    }

    LaunchedEffect(uiState.isPaired) {
        if (uiState.isPaired) {
            onPairingComplete()
        }
    }

    LaunchedEffect(uiState.mode) {
        if (uiState.mode == PairingMode.QR_CODE && !hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmakaColors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Pair Your Device",
            style = MaterialTheme.typography.headlineLarge,
            color = AmakaColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan the QR code or enter the pairing code from the web app",
            style = MaterialTheme.typography.bodyLarge,
            color = AmakaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmakaColors.surface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ModeToggleButton(
                text = "QR Code",
                icon = Icons.Default.CameraAlt,
                selected = uiState.mode == PairingMode.QR_CODE,
                onClick = { viewModel.setMode(PairingMode.QR_CODE) },
                modifier = Modifier.weight(1f)
            )
            ModeToggleButton(
                text = "Manual Code",
                icon = Icons.Default.Keyboard,
                selected = uiState.mode == PairingMode.MANUAL_CODE,
                onClick = { viewModel.setMode(PairingMode.MANUAL_CODE) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content based on mode
        when (uiState.mode) {
            PairingMode.QR_CODE -> {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmakaColors.surface)
                    ) {
                        QRCodeScanner(
                            onQRCodeScanned = { code ->
                                viewModel.onQRCodeScanned(code)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AmakaColors.background.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AmakaColors.accentBlue)
                            }
                        }
                    }
                } else {
                    CameraPermissionRequest(
                        onRequestPermission = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            }

            PairingMode.MANUAL_CODE -> {
                ManualCodeEntry(
                    code = uiState.manualCode,
                    onCodeChange = viewModel::updateManualCode,
                    onSubmit = viewModel::submitManualCode,
                    isLoading = uiState.isLoading
                )
            }
        }

        // Error message
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AmakaColors.accentRed.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = error,
                    color = AmakaColors.accentRed,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Visit app.amakaflow.com to get your pairing code",
            style = MaterialTheme.typography.bodySmall,
            color = AmakaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        // Developer testing option (debug builds only)
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { showTestModeDialog = true }
            ) {
                Text(
                    text = "Skip for E2E Testing",
                    color = AmakaColors.accentOrange,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Test mode configuration dialog
    if (showTestModeDialog) {
        AlertDialog(
            onDismissRequest = { showTestModeDialog = false },
            title = { Text("Enable E2E Test Mode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AmakaSpacing.md.dp)) {
                    Text(
                        text = "Enter test credentials to bypass authentication for E2E testing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmakaColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(AmakaSpacing.sm.dp))
                    OutlinedTextField(
                        value = testAuthSecret,
                        onValueChange = { testAuthSecret = it },
                        label = { Text("Auth Secret") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmakaColors.accentBlue,
                            unfocusedBorderColor = AmakaColors.textTertiary,
                            focusedLabelColor = AmakaColors.accentBlue,
                            unfocusedLabelColor = AmakaColors.textSecondary,
                            cursorColor = AmakaColors.accentBlue,
                            focusedTextColor = AmakaColors.textPrimary,
                            unfocusedTextColor = AmakaColors.textPrimary
                        )
                    )
                    OutlinedTextField(
                        value = testUserId,
                        onValueChange = { testUserId = it },
                        label = { Text("User ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmakaColors.accentBlue,
                            unfocusedBorderColor = AmakaColors.textTertiary,
                            focusedLabelColor = AmakaColors.accentBlue,
                            unfocusedLabelColor = AmakaColors.textSecondary,
                            cursorColor = AmakaColors.accentBlue,
                            focusedTextColor = AmakaColors.textPrimary,
                            unfocusedTextColor = AmakaColors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (testAuthSecret.isNotBlank() && testUserId.isNotBlank()) {
                            testConfig.enableTestMode(testAuthSecret, testUserId)
                            testAuthSecret = ""
                            testUserId = ""
                            showTestModeDialog = false
                            onPairingComplete()
                        }
                    },
                    enabled = testAuthSecret.isNotBlank() && testUserId.isNotBlank()
                ) {
                    Text("Enable & Skip Pairing")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestModeDialog = false }) {
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
private fun ModeToggleButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) AmakaColors.accentBlue else AmakaColors.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AmakaColors.textPrimary else AmakaColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (selected) AmakaColors.textPrimary else AmakaColors.textSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ManualCodeEntry(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AmakaColors.surface, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter 6-Character Code",
            style = MaterialTheme.typography.titleMedium,
            color = AmakaColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ABC123") },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AmakaColors.accentBlue,
                unfocusedBorderColor = AmakaColors.textSecondary.copy(alpha = 0.3f),
                focusedTextColor = AmakaColors.textPrimary,
                unfocusedTextColor = AmakaColors.textPrimary,
                cursorColor = AmakaColors.accentBlue
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = code.length == 6 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmakaColors.accentBlue,
                disabledContainerColor = AmakaColors.accentBlue.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AmakaColors.textPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Pair Device",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AmakaColors.surface, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AmakaColors.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.titleMedium,
            color = AmakaColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Allow camera access to scan QR codes",
            style = MaterialTheme.typography.bodyMedium,
            color = AmakaColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = AmakaColors.accentBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Grant Permission")
        }
    }
}
