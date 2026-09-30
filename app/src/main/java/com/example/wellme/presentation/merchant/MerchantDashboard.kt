package com.example.wellme.presentation.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.Transaction
import com.example.wellme.theme.WellMeTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MerchantDashboard(
    merchant: MerchantProfile?,
    progress: Float,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val raisedKsh = merchant?.let { it.poolRaisedInCents / 100.0 } ?: 0.0
    val targetKsh = merchant?.let { it.poolTargetInCents / 100.0 } ?: 0.0
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Financial Impact",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006D3E),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        FundingProgressCard(
            raisedAmount = raisedKsh,
            targetAmount = targetKsh,
            progress = progress,
            modifier = Modifier.padding(bottom = 32.dp)
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
                tint = Color(0xFF006D3E),
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
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Verified",
                            color = Color(0xFF2E7D32),
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
                color = Color(0xFF006D3E)
            )
        }
    }
}

@Composable
fun FundingProgressCard(
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
            Text(
                text = stringResource(R.string.funding_progress),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F9D58),
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
                        color = Color(0xFF00391C)
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
                color = Color(0xFF006D3E).copy(alpha = 0.08f),
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
                        tint = Color(0xFF006D3E)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.funded_percentage, (progress * 100).toInt()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF006D3E)
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
            merchant = MerchantProfile(
                merchantId = "m1",
                businessName = "Campus Cafe",
                discountTier = 0.15,
                poolTargetInCents = 200000L,
                poolRaisedInCents = 120000L,
                isVerified = true
            ),
            progress = 0.6f,
            transactions = listOf(
                Transaction(
                    transactionId = "tx1",
                    amountInCents = 45000,
                    originalAmountInCents = 50000,
                    discountAppliedInCents = 5000,
                    timestamp = System.currentTimeMillis(),
                    type = com.example.wellme.domain.model.TransactionType.PAYMENT,
                    merchantId = "m1",
                    studentId = "s1"
                )
            )
        )
    }
}
