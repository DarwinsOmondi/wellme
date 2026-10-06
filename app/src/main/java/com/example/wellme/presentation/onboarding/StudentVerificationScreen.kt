package com.example.wellme.presentation.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentVerificationScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onVerifyClick: () -> Unit = {},
    onLaterClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val name = uri?.lastPathSegment ?: "student_id.jpg"
        viewModel.onFileSelected(uri, name)
    }

    LaunchedEffect(state) {
        when (state) {
            is OnboardingState.Success -> onVerifyClick()
            is OnboardingState.Error -> {
                snackbarHostState.showSnackbar((state as OnboardingState.Error).message)
                viewModel.clearError()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        StudentVerificationContent(
            selectedFileUri = viewModel.selectedFileUri,
            fileName = viewModel.fileName,
            isUploading = viewModel.isUploading || state is OnboardingState.Loading,
            onFileClick = { launcher.launch("image/*") },
            onClearFile = { viewModel.clearSelectedFile() },
            onBackClick = onBackClick,
            onVerifyClick = {
                viewModel.submitStudentKyc(context.contentResolver)
            },
            onLaterClick = onLaterClick,
            modifier = modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentVerificationContent(
    selectedFileUri: android.net.Uri?,
    fileName: String,
    isUploading: Boolean,
    onFileClick: () -> Unit,
    onClearFile: () -> Unit,
    onBackClick: () -> Unit = {},
    onVerifyClick: () -> Unit = {},
    onLaterClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            MerchantOnboardingTopBar(onBackClick = onBackClick)
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.verification),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                )
                Text(
                    text = stringResource(id = R.string.step_2_of_2),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(EmeraldDark, RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(id = R.string.upload_student_id),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1A),
                textAlign = TextAlign.Center,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.upload_student_id_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4A4A4A).copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Upload Box with Dashed Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF9FAFB))
                    .clickable { onFileClick() }
                    .padding(24.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = Color(0xFF9CA3AF),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = EmeraldDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(id = R.string.digital_student_id_photo),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1A),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.drag_and_drop_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Uploading Status Card
            if (isUploading || selectedFileUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Placeholder for image preview
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_student),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fileName.ifEmpty { "student_id_photo.jpg" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C1A),
                                    maxLines = 1,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (selectedFileUri != null) "100%" else "75%",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { if (selectedFileUri != null) 1f else 0.75f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = EmeraldDark,
                                trackColor = Color(0xFFE5E7EB)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedFileUri != null) "Upload complete" else stringResource(id = R.string.uploading_to_secure_vault),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = onClearFile) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color(0xFF4A4A4A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Buttons
            Button(
                onClick = onVerifyClick,
                enabled = selectedFileUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldDark,
                    contentColor = Color.White,
                    disabledContainerColor = EmeraldDark.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.verify_identity),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLaterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, EmeraldDark),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmeraldDark
                )
            ) {
                Text(
                    text = stringResource(id = R.string.ill_do_this_later),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentVerificationScreenPreview() {
    WellMeTheme {
        StudentVerificationContent(
            selectedFileUri = null,
            fileName = "",
            isUploading = false,
            onFileClick = {},
            onClearFile = {}
        )
    }
}
