package com.example.wellme.presentation.merchant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.domain.model.LoanLifecycleState
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueDark
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RequestCapitalTab(
    amountInput: String,
    onAmountInputChange: (String) -> Unit,
    selectedYield: Double,
    onSelectedYieldChange: (Double) -> Unit,
    loanState: LoanLifecycleState,
    onSubmit: () -> Unit,
    onViewLoansHistory: () -> Unit = {},
    onDismissStatus: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val yields = listOf(0.10, 0.15, 0.20, 0.25, 0.30)
    val isEnabled = loanState is LoanLifecycleState.Idle || loanState is LoanLifecycleState.OperationalError
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Request Funding",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        // Loan Request Status
        if (loanState !is LoanLifecycleState.Idle) {
            StatusCard(
                loanState = loanState,
                onDismiss = onDismissStatus
            )
        }

        // Loan Request Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Funding Amount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = "Enter the KSh amount you need for your business expansion.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = onAmountInputChange,
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    prefix = {
                        Text(
                            "KSh",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1E),
                        fontSize = 24.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            }
        }

        // Discount Yield Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yield Offered",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C1E)
                    )
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Select the discount percentage offered to community lenders as a thank you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    yields.forEach { yield ->
                        val isSelected = selectedYield == yield
                        Surface(
                            onClick = { if (isEnabled) onSelectedYieldChange(yield) },
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            color = if (isSelected) PrimaryBlue else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp
                        ) {
                            Text(
                                text = "${(yield * 100).toInt()}% Yield",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSubmit,
                    enabled = isEnabled && amountInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Submit Funding Request",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onViewLoansHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.5.dp, PrimaryBlue)
                ) {
                    Text(
                        text = "View Capital History & Total Loans",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun StatusCard(
    loanState: LoanLifecycleState,
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (loanState) {
                is LoanLifecycleState.OperationalError -> Color(0xFFFFEBEE)
                is LoanLifecycleState.DisbursedSuccess -> PrimaryBlueLight
                else -> Color(0xFFE3F2FD)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (loanState) {
                is LoanLifecycleState.SubmittingRpc -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1976D2))
                    Text("Connecting to Secure Network...", fontWeight = FontWeight.Medium)
                }
                is LoanLifecycleState.RequestAccepted -> {
                    Text("Request Accepted!", fontWeight = FontWeight.ExtraBold, color = PrimaryBlueDark)
                    Text("Transaction ID: ${loanState.loanId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(6.dp), color = PrimaryBlue, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                    Text("Processing disbursement...", fontSize = 13.sp)
                }
                is LoanLifecycleState.DisbursedSuccess -> {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(36.dp))
                    Text("Disbursement Successful!", fontWeight = FontWeight.ExtraBold, color = PrimaryBlueDark)
                    Text("KSh ${loanState.amountInCents / 100} deposited to your M-Pesa.", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
                is LoanLifecycleState.OperationalError -> {
                    Text("Network Error", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                    Text(loanState.message, style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestCapitalTabPreview() {
    WellMeTheme {
        RequestCapitalTab(
            amountInput = "25000",
            onAmountInputChange = {},
            selectedYield = 0.20,
            onSelectedYieldChange = {},
            loanState = LoanLifecycleState.Idle,
            onSubmit = {},
            onViewLoansHistory = {},
            onDismissStatus = {}
        )
    }
}
