package com.amakaflow.companion.ui.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@Composable
fun QRCodeScanner(
    onQRCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        cameraProvider?.let { provider ->
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                Executors.newSingleThreadExecutor(),
                                QRCodeAnalyzer { qrCode ->
                                    onQRCodeScanned(qrCode)
                                }
                            )
                        }

                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("QRCodeScanner", "Camera binding failed", e)
                    }

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay with scanning frame
        ScannerOverlay()
    }
}

@Composable
private fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scannerSize = size.minDimension * 0.7f
        val left = (size.width - scannerSize) / 2
        val top = (size.height - scannerSize) / 2

        // Semi-transparent background
        drawRect(
            color = Color.Black.copy(alpha = 0.6f),
            size = size
        )

        // Clear the scanner area
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(scannerSize, scannerSize),
            cornerRadius = CornerRadius(16f, 16f),
            blendMode = BlendMode.Clear
        )

        // Draw corner brackets
        val bracketLength = 40f
        val bracketWidth = 4f
        val cornerRadius = 16f
        val bracketColor = Color.White

        // Top-left corner
        drawLine(
            color = bracketColor,
            start = Offset(left + cornerRadius, top),
            end = Offset(left + bracketLength, top),
            strokeWidth = bracketWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(left, top + cornerRadius),
            end = Offset(left, top + bracketLength),
            strokeWidth = bracketWidth
        )

        // Top-right corner
        drawLine(
            color = bracketColor,
            start = Offset(left + scannerSize - bracketLength, top),
            end = Offset(left + scannerSize - cornerRadius, top),
            strokeWidth = bracketWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(left + scannerSize, top + cornerRadius),
            end = Offset(left + scannerSize, top + bracketLength),
            strokeWidth = bracketWidth
        )

        // Bottom-left corner
        drawLine(
            color = bracketColor,
            start = Offset(left + cornerRadius, top + scannerSize),
            end = Offset(left + bracketLength, top + scannerSize),
            strokeWidth = bracketWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(left, top + scannerSize - bracketLength),
            end = Offset(left, top + scannerSize - cornerRadius),
            strokeWidth = bracketWidth
        )

        // Bottom-right corner
        drawLine(
            color = bracketColor,
            start = Offset(left + scannerSize - bracketLength, top + scannerSize),
            end = Offset(left + scannerSize - cornerRadius, top + scannerSize),
            strokeWidth = bracketWidth
        )
        drawLine(
            color = bracketColor,
            start = Offset(left + scannerSize, top + scannerSize - bracketLength),
            end = Offset(left + scannerSize, top + scannerSize - cornerRadius),
            strokeWidth = bracketWidth
        )
    }
}

private class QRCodeAnalyzer(
    private val onQRCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()
    private var lastScannedCode: String? = null
    private var lastScanTime = 0L

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        // Debounce scanning (wait 1 second between scans)
        if (currentTime - lastScanTime < 1000) {
            image.close()
            return
        }

        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val source = PlanarYUVLuminanceSource(
            bytes,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )

        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(binaryBitmap)
            val code = result.text

            // Only trigger callback if code is different from last scan
            if (code != lastScannedCode) {
                lastScannedCode = code
                lastScanTime = currentTime
                onQRCodeScanned(code)
            }
        } catch (_: NotFoundException) {
            // No QR code found in image
        } catch (e: Exception) {
            Log.e("QRCodeAnalyzer", "Error analyzing image", e)
        } finally {
            image.close()
        }
    }
}
