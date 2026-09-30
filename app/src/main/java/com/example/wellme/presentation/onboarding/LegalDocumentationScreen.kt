package com.example.wellme.presentation.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
fun LegalDocumentationScreen(
    viewModel: OnboardingViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    var activePicker by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            activePicker?.let { type ->
                viewModel.onDocumentSelected(type, it)
            }
        }
        activePicker = null
    }

    LegalDocumentationContent(
        nationalIdUri = viewModel.nationalIdUri,
        businessCertUri = viewModel.businessCertUri,
        businessPermitUri = viewModel.businessPermitUri,
        onFileSelect = { type ->
            activePicker = type
            launcher.launch("*/*")
        },
        onBackClick = onBackClick,
        onSaveDraft = onSaveDraft,
        onContinue = onContinue,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDocumentationContent(
    nationalIdUri: Uri?,
    businessCertUri: Uri?,
    businessPermitUri: Uri?,
    onFileSelect: (String) -> Unit,
    onBackClick: () -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onContinue: () -> Unit = {},
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
        containerColor = Color(0xFFF9FAFB),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.legal_documentation),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1A),
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.verify_business_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A4A4A),
                            lineHeight = 20.sp
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.step_2_of_3),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .height(6.dp)
                            .background(EmeraldDark, RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Document Cards
                DocumentUploadCard(
                    title = stringResource(id = R.string.national_id_card),
                    description = stringResource(id = R.string.national_id_card_desc),
                    icon = Icons.Default.ContactPage,
                    iconBackgroundColor = Color(0xFF2DE3A3).copy(alpha = 0.5f),
                    additionalInfo = stringResource(id = R.string.accepted_files_info),
                    selectedFileName = nationalIdUri?.lastPathSegment,
                    onSelectFile = { onFileSelect("national_id") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DocumentUploadCard(
                    title = stringResource(id = R.string.business_registration_cert),
                    description = stringResource(id = R.string.business_registration_cert_desc),
                    icon = Icons.Default.AccountBalance,
                    iconBackgroundColor = Color(0xFFFFD1A1).copy(alpha = 0.5f),
                    selectedFileName = businessCertUri?.lastPathSegment,
                    onSelectFile = { onFileSelect("business_cert") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                DocumentUploadCard(
                    title = stringResource(id = R.string.county_business_permit),
                    description = stringResource(id = R.string.county_business_permit_desc),
                    icon = Icons.Default.Assignment,
                    iconBackgroundColor = Color(0xFFE5E7EB).copy(alpha = 0.5f),
                    selectedFileName = businessPermitUri?.lastPathSegment,
                    onSelectFile = { onFileSelect("business_permit") }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Why do we need this section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF1F2937)) // Darker background
                    .padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = R.string.why_do_we_need_this),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.legal_doc_explanation),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF065F46),
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_security),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldDark),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldDark)
                ) {
                    Text(
                        text = stringResource(id = R.string.save_draft),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onContinue,
                    enabled = nationalIdUri != null && businessCertUri != null && businessPermitUri != null,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldDark,
                        disabledContainerColor = EmeraldDark.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.continue_to_step_3),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentUploadCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    additionalInfo: String? = null,
    selectedFileName: String? = null,
    onSelectFile: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = iconBackgroundColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        fontSize = 20.sp
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280),
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onSelectFile,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedFileName != null) EmeraldGreen else EmeraldDark),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    Icon(
                        imageVector = if (selectedFileName != null) Icons.Default.Check else Icons.Default.PostAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (selectedFileName != null) "File Selected" else stringResource(id = R.string.select_file),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (selectedFileName != null) {
                    Text(
                        text = selectedFileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldDark,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
            
            if (additionalInfo != null && selectedFileName == null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = additionalInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LegalDocumentationScreenPreview() {
    WellMeTheme {
        LegalDocumentationContent(
            nationalIdUri = null,
            businessCertUri = null,
            businessPermitUri = null,
            onFileSelect = {}
        )
    }
}
