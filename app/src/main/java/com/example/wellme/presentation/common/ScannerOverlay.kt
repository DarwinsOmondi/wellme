package com.example.wellme.presentation.common

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun ScannerOverlay(
    onClose: () -> Unit,
    onScan: (String, Long) -> Unit,
    showSimulateButton: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControlState by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var showManualInputDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Live Camera Feed via CameraX
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = androidx.camera.core.Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val barcodeScanner = BarcodeScanning.getClient()
                            val executor = Executors.newSingleThreadExecutor()

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(executor) { imageProxy ->
                                        @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            barcodeScanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val rawValue: String? = barcode.rawValue
                                                        if (!rawValue.isNullOrBlank()) {
                                                            Log.d("ScannerOverlay", "QR Detected: $rawValue")
                                                            val (merchantId, amountInCents) = parseQrData(rawValue)
                                                            onScan(merchantId, amountInCents)
                                                            break
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraControlState = camera.cameraControl
                        } catch (e: Exception) {
                            Log.e("ScannerOverlay", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission Request Screen
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "WellMe needs camera access to scan vendor QR codes for instant payments.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Top Control Bar Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = {
                        isFlashOn = !isFlashOn
                        cameraControlState?.enableTorch(isFlashOn)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isFlashOn) Color(0xFF006D3E) else Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flash",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { showManualInputDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Manual Code",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Scanning Target Frame
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                val bracketSize = 48.dp
                val stroke = 4.dp
                val color = Color(0xFF006D3E)
                val radius = 16.dp

                // Top Left
                Box(Modifier.align(Alignment.TopStart).size(bracketSize)) {
                    Box(Modifier.height(stroke).width(bracketSize).background(color, RoundedCornerShape(radius)))
                    Box(Modifier.width(stroke).height(bracketSize).background(color, RoundedCornerShape(radius)))
                }
                // Top Right
                Box(Modifier.align(Alignment.TopEnd).size(bracketSize)) {
                    Box(Modifier.height(stroke).width(bracketSize).align(Alignment.TopEnd).background(color, RoundedCornerShape(radius)))
                    Box(Modifier.width(stroke).height(bracketSize).align(Alignment.TopEnd).background(color, RoundedCornerShape(radius)))
                }
                // Bottom Left
                Box(Modifier.align(Alignment.BottomStart).size(bracketSize)) {
                    Box(Modifier.height(stroke).width(bracketSize).align(Alignment.BottomStart).background(color, RoundedCornerShape(radius)))
                    Box(Modifier.width(stroke).height(bracketSize).align(Alignment.BottomStart).background(color, RoundedCornerShape(radius)))
                }
                // Bottom Right
                Box(Modifier.align(Alignment.BottomEnd).size(bracketSize)) {
                    Box(Modifier.height(stroke).width(bracketSize).align(Alignment.BottomEnd).background(color, RoundedCornerShape(radius)))
                    Box(Modifier.width(stroke).height(bracketSize).align(Alignment.BottomEnd).background(color, RoundedCornerShape(radius)))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Align vendor QR code inside frame",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Manual Input Dialog
        if (showManualInputDialog) {
            ManualPaymentDialog(
                onDismiss = { showManualInputDialog = false },
                onConfirm = { merchantId, amountKsh ->
                    showManualInputDialog = false
                    onScan(merchantId, (amountKsh * 100).toLong())
                }
            )
        }
    }
}

fun parseQrData(data: String): Pair<String, Long> {
    return try {
        if (data.contains("wellme_pay://")) {
            val uri = Uri.parse(data)
            val merchantId = uri.host ?: uri.path?.removePrefix("/") ?: "campus_cafe"
            val amountKsh = uri.getQueryParameter("amount")?.toDoubleOrNull() ?: 100.0
            Pair(merchantId, (amountKsh * 100).toLong())
        } else if (data.contains(":")) {
            val parts = data.split(":")
            val merchantId = parts[0]
            val amountCents = parts.getOrNull(1)?.toLongOrNull() ?: 10000L
            Pair(merchantId, amountCents)
        } else if (data.contains("?")) {
            val parts = data.split("?")
            val merchantId = parts[0]
            val amountKsh = parts[1].replace("amount=", "").toDoubleOrNull() ?: 100.0
            Pair(merchantId, (amountKsh * 100).toLong())
        } else {
            Pair(data.ifBlank { "campus_cafe" }, 10000L)
        }
    } catch (_: Exception) {
        Pair("campus_cafe", 10000L)
    }
}

@Composable
fun ManualPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var merchantId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Payment Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = merchantId,
                    onValueChange = { merchantId = it },
                    label = { Text("Vendor / Merchant ID") },
                    placeholder = { Text("e.g. campus_cafe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Amount (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (merchantId.isNotBlank() && amountVal > 0) {
                        onConfirm(merchantId, amountVal)
                    }
                },
                enabled = merchantId.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E))
            ) {
                Text("Process Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
