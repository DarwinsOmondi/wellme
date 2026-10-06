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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
fun FinancialSettlementScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        val name = uri?.lastPathSegment ?: "mpesa_document.pdf"
        viewModel.onFileSelected(uri, name)
    }

    LaunchedEffect(state) {
        when (state) {
            is OnboardingState.Success -> onComplete()
            is OnboardingState.Error -> {
                snackbarHostState.showSnackbar((state as OnboardingState.Error).message)
                viewModel.clearError()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        FinancialSettlementContent(
            tillNumber = viewModel.tillNumber,
            onTillNumberChange = viewModel::onTillNumberChange,
            selectedFileUri = viewModel.selectedFileUri,
            fileName = viewModel.fileName,
            onFileClick = { launcher.launch("*/*") },
            onBackClick = onBackClick,
            onComplete = {
                viewModel.submitMerchantKyc(context.contentResolver)
            },
            modifier = modifier // padding handled in content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialSettlementContent(
    tillNumber: String,
    onTillNumberChange: (String) -> Unit,
    selectedFileUri: android.net.Uri?,
    fileName: String,
    onFileClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            MerchantOnboardingTopBar(
                onBackClick = onBackClick,
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.step_3_of_3),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
                Text(
                    text = stringResource(id = R.string.final_stage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar (Full)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(EmeraldDark, RoundedCornerShape(3.dp))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.financial_settlement),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1A),
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.financial_settlement_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4A4A4A).copy(alpha = 0.8f),
                lineHeight = 24.sp,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Secure Verification Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0D1B2A),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.secure_verification),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D1B2A),
                            fontSize = 16.sp
                        )
                        Text(
                            text = stringResource(id = R.string.secure_verification_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0D1B2A).copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.mpesa_till_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tillNumber,
                onValueChange = onTillNumberChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.mpesa_till_placeholder),
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.mpesa_till_helper),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.official_mpesa_document),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Upload Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF9FAFB))
                    .clickable { onFileClick() }
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = if (selectedFileUri != null) EmeraldGreen else Color(0xFFD1D5DB),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (selectedFileUri != null) EmeraldGreen else EmeraldDark,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selectedFileUri != null) Icons.Default.Check else Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedFileUri != null) "File Selected" else stringResource(id = R.string.tap_to_upload),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedFileUri != null) fileName else stringResource(id = R.string.mpesa_document_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectedFileUri != null) EmeraldDark else Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Complete Button
            Button(
                onClick = onComplete,
                enabled = tillNumber.length >= 5 && selectedFileUri != null,
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
                        text = stringResource(id = R.string.complete_onboarding),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.merchant_terms_agreement),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FinancialSettlementScreenPreview() {
    WellMeTheme {
        FinancialSettlementContent(
            tillNumber = "",
            onTillNumberChange = {},
            selectedFileUri = null,
            fileName = "",
            onFileClick = {}
        )
    }
}
