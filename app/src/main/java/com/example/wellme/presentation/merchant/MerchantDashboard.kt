package com.example.wellme.presentation.merchant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.R
import com.example.wellme.domain.model.Transaction
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueDark
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme
import com.example.wellme.presentation.security.SecurityViewModel
import com.example.wellme.util.BiometricAuthManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Fingerprint
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboard(
    viewModel: MerchantViewModel = hiltViewModel(),
    securityViewModel: SecurityViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var showTargetUpdateDialog by remember { mutableStateOf(false) }
    val merchant by viewModel.merchant.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val progress by viewModel.poolProgress.collectAsState()
    val isViewModelRefreshing by viewModel.isRefreshing.collectAsState()
    val isWalletLocked by securityViewModel.isWalletLocked.collectAsState()
    val context = LocalContext.current
    val raisedKsh = if (transactions.isNotEmpty()) {
        transactions.sumOf { it.amountInCents } / 100.0
    } else {
        merchant?.let { it.poolRaisedInCents / 100.0 } ?: 0.0
    }
    val targetKsh = merchant?.let { it.poolTargetInCents / 100.0 } ?: 0.0
    PullToRefreshBox(
        isRefreshing = isViewModelRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Financial Impact",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                if (isWalletLocked) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Merchant Financials Locked",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Verify fingerprint, face ID or PIN to view balance and ledger",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    BiometricAuthManager.authenticate(
                                        context = context,
                                        title = "Unlock Merchant Financials",
                                        subtitle = "Verify fingerprint or PIN",
                                        onSuccess = { securityViewModel.unlockWallet() },
                                        onError = {}
                                    )
                                },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unlock with Biometric",
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    FundingProgressCard(
                        onUpdateTarget = {
                            showTargetUpdateDialog = true
                        },
                        raisedAmount = raisedKsh,
                        targetAmount = targetKsh,
                        progress = progress,
                        modifier = Modifier.padding(bottom = 32.dp),
                    )

                    // Live Payout Stream Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = "Receipt icon",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Live Ledger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                    }

                    // Incoming transactions list
                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Waiting for customer checkouts...\nTransactions will appear here in real-time.",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(transactions) { tx ->
                                PayoutStreamItem(transaction = tx)
                            }
                        }
                    }
                }
            }

            if (showTargetUpdateDialog) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showTargetUpdateDialog = false
                    },
                    sheetState = rememberModalBottomSheetState(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Update Your Target Goal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E)
                        )
                        OutlinedTextField(
                            value = viewModel.target,
                            onValueChange = viewModel::onTargetChange,
                            label = { Text("New Target (KSh)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.updateTarget()
                                showTargetUpdateDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            Text(
                                text = "Update Target",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PayoutStreamItem(transaction: Transaction) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = dateFormat.format(Date(transaction.timestamp))

    val maskedStudent = transaction.studentId?.let { id ->
        if (id.length > 5) "Student ***${id.takeLast(4)}" else "Student ***"
    } ?: "Student ***"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = maskedStudent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1C1E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = PrimaryBlueLight,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Verified",
                            color = PrimaryBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Time: $timeString • Discount processed",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                text = String.format("+KSh %,.2f", transaction.amountInCents / 100.0),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = PrimaryBlue
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundingProgressCard(
    onUpdateTarget: () -> Unit,
    raisedAmount: Double,
    targetAmount: Double,
    progress: Float,
    modifier: Modifier = Modifier
) {


    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(R.string.funding_progress),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                IconButton(
                    onClick = {
                        onUpdateTarget()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = PrimaryBlueLight,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Icon(imageVector = Icons.Filled.AutoGraph, contentDescription = "Target")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryBlue,
                    strokeWidth = 18.dp,
                    trackColor = Color(0xFFF1F4F9),
                    strokeCap = StrokeCap.Round,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("KSh %,.0f", raisedAmount),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = PrimaryBlueDark
                    )
                    Text(
                        text = stringResource(
                            R.string.raised_of_target,
                            "KSh",
                            String.format("%,.0f", targetAmount)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                color = PrimaryBlue.copy(alpha = 0.08f),
                shape = RoundedCornerShape(100.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trending_up),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.funded_percentage, (progress * 100).toInt()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MerchantDashboardPreview() {
    WellMeTheme {
        MerchantDashboard(
        )
    }
}
