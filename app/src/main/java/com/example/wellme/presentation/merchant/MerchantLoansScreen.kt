package com.example.wellme.presentation.merchant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wellme.domain.model.MerchantLoanDto
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantLoansScreen(
    merchantLoans: List<MerchantLoanDto>,
    totalLoanAmountKsh: Double,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Capital Request History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Total Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL CAPITAL REQUESTED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "KSh %,.2f", totalLoanAmountKsh),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${merchantLoans.size} Total Loan Requests",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Requested Loans Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (merchantLoans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No capital requests submitted yet.\nYour loan requests will appear here.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(merchantLoans, key = { it.id }) { loan ->
                        MerchantLoanItemCard(loan = loan)
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantLoanItemCard(loan: MerchantLoanDto) {
    val isDisbursed = loan.status.equals("DISBURSED", ignoreCase = true)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (isDisbursed) PrimaryBlueLight else Color(0xFFFFF3E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isDisbursed) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (isDisbursed) PrimaryBlue else Color(0xFFEF6C00),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "KSh ${String.format(Locale.getDefault(), "%,d", loan.amountRequestedInCents / 100)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Till 174379 • Ref: ${loan.id.take(8).uppercase()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Surface(
                color = if (isDisbursed) PrimaryBlueLight else Color(0xFFFFF3E0),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isDisbursed) "DISBURSED" else "PENDING",
                    color = if (isDisbursed) PrimaryBlue else Color(0xFFEF6C00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MerchantLoansScreenPreview() {
    WellMeTheme {
        MerchantLoansScreen(
            merchantLoans = listOf(
                MerchantLoanDto("l1", "m1", 2500000L, "DISBURSED", "REQ-1", "2024-03-01"),
                MerchantLoanDto("l2", "m1", 5000000L, "PENDING", "REQ-2", "2024-03-02")
            ),
            totalLoanAmountKsh = 75000.0,
            onBack = {}
        )
    }
}
