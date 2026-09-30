package com.example.wellme.presentation.student

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import com.example.wellme.R
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType
import com.example.wellme.presentation.common.BottomSheetContent
import com.example.wellme.presentation.common.ScannerOverlay
import com.example.wellme.theme.WellMeTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onDiscoverClick: () -> Unit = {}
) {
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val sheetState by viewModel.sheetState.collectAsState()
    val studentKyc by viewModel.studentKyc.collectAsState()

    StudentContent(
        wallet = wallet,
        transactions = transactions,
        sheetState = sheetState,
        studentKyc = studentKyc,
        onScan = { merchantId, amount -> viewModel.scanMerchant(merchantId, amount) },
        onResetScanner = { viewModel.resetScanner() },
        onConfirmPayment = { viewModel.confirmPayment() },
        onDeposit = { amount, phone -> viewModel.initiateDeposit(amount, phone) },
        onRefresh = { viewModel.refreshWallet() },
        onProfileClick = onProfileClick,
        onDiscoverClick = onDiscoverClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentContent(
    wallet: StudentWallet?,
    transactions: List<Transaction>,
    sheetState: SheetState,
    studentKyc: com.example.wellme.data.remote.model.StudentKyc?,
    onScan: (String, Long) -> Unit,
    onResetScanner: () -> Unit,
    onConfirmPayment: () -> Unit,
    onDeposit: (Double, String) -> Unit,
    onRefresh: () -> Unit,
    onProfileClick: () -> Unit = {},
    onDiscoverClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isScanning by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    val balanceKsh = wallet?.let { it.balanceInCents / 100.0 } ?: 0.0

    val initials = remember(studentKyc?.studentName) {
        val name = studentKyc?.studentName ?: ""
        if (name.isBlank()) ".."
        else name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                StudentHeader(
                    initials = initials,
                    onProfileClick = onProfileClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                BalanceCard(
                    balance = String.format(Locale.getDefault(), "KSh %,.2f", balanceKsh),
                    onDepositClick = { showDepositDialog = true },
                    onRefreshClick = onRefresh
                )

                SecurityInfoBar()

                Spacer(modifier = Modifier.height(16.dp))
                
                DiscoverVendorsBanner(onClick = onDiscoverClick)

                Spacer(modifier = Modifier.height(8.dp))

                RecentActivitySection(
                    transactions = transactions
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            BottomActionButton(
                onClick = { isScanning = true },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp, top = 16.dp)
            )
        }

        if (showDepositDialog) {
            DepositDialog(
                initialPhoneNumber = studentKyc?.phoneNumber ?: "254",
                onDismiss = { showDepositDialog = false },
                onConfirm = { amount, phone ->
                    showDepositDialog = false
                    onDeposit(amount, phone)
                }
            )
        }

        // Payment Confirmation Screen (includes Scanner and BottomSheet)
        AnimatedVisibility(
            visible = isScanning || sheetState !is SheetState.Idle,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PaymentConfirmationScreen(
                state = sheetState,
                onClose = {
                    isScanning = false
                    onResetScanner()
                },
                onScan = { merchantId, amount ->
                    isScanning = false
                    onScan(merchantId, amount)
                },
                onConfirm = onConfirmPayment
            )
        }
    }
}

@Composable
fun StudentHeader(
    modifier: Modifier = Modifier,
    initials: String = "JD",
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))
                    )
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006D3E)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_notifications),
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BalanceCard(
    balance: String,
    modifier: Modifier = Modifier,
    onDepositClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00391C))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.available_balance),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_wallet),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = balance,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onRefreshClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = "Refresh Wallet",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.deposit),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF006D3E)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_horiz),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecurityInfoBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE0F2F1)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                tint = Color(0xFF006D3E),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.secured_for_food_access),
                color = Color(0xFF006D3E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RecentActivitySection(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_activity),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* TODO */ }) {
                Text(
                    text = stringResource(R.string.see_all),
                    color = Color(0xFF006D3E),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent activity found.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            transactions.take(3).forEach { transaction ->
                TransactionItem(transaction = transaction)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.merchantId == "mpesa") Color(0xFFE8F5E9)
                        else Color.LightGray.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (transaction.merchantId == "mpesa") R.drawable.ic_account_balance
                             else R.drawable.ic_restaurant
                    ),
                    contentDescription = null,
                    tint = if (transaction.merchantId == "mpesa") Color(0xFF388E3C)
                           else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.merchantId == "mpesa") stringResource(R.string.mpesa_top_up)
                           else "Merchant: ${transaction.merchantId?.take(8)?.uppercase() ?: "..."}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (transaction.merchantId != "mpesa") {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_security),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Verified Transaction",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()) }
                        Text(
                            text = dateFormat.format(java.util.Date(transaction.timestamp)),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (transaction.type == TransactionType.STIPEND)
                        String.format(
                            Locale.getDefault(),
                            "+KSh %,d",
                            transaction.amountInCents / 100
                        )
                    else String.format(
                        Locale.getDefault(),
                        "-KSh %,d",
                        transaction.amountInCents / 100
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (transaction.type == TransactionType.STIPEND) Color(0xFF388E3C) else Color.Black
                )
                Icon(
                    painter = painterResource(
                        id = if (transaction.type == TransactionType.STIPEND)
                            R.drawable.ic_arrow_upward
                        else R.drawable.ic_arrow_downward
                    ),
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.STIPEND) Color(0xFF388E3C) else Color(
                        0xFFD32F2F
                    ),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun BottomActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_qr_code),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.scan_to_pay_save),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun DepositDialog(
    initialPhoneNumber: String = "254",
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(if (initialPhoneNumber.isNotBlank()) initialPhoneNumber else "254") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit via M-Pesa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                    label = { Text("Amount (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("2547XXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "You will receive an STK Push on your phone to authorize the payment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble != null && phone.isNotBlank()) {
                        onConfirm(amountDouble, phone)
                    }
                },
                enabled = amount.isNotBlank() && phone.length >= 10,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E))
            ) {
                Text("Proceed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun DiscoverVendorsBanner(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF006D3E),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Discover Vendors",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Browse local food stalls and get instant discounts.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_store),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentScreenPreview() {
    WellMeTheme {
        StudentContent(
            wallet = StudentWallet(
                studentId = "student_123",
                balanceInCents = 245000L
            ),
            transactions = emptyList(),
            sheetState = SheetState.Idle,
            studentKyc = com.example.wellme.data.remote.model.StudentKyc(
                id = "student_123",
                studentIdNumber = "123456",
                studentName = "Darwin WellMe",
                campusEmail = "darwin@campus.com",
                phoneNumber = "254712345678"
            ),
            onScan = { _, _ -> },
            onResetScanner = {},
            onConfirmPayment = {},
            onDeposit = { _, _ -> },
            onRefresh = {}
        )
    }
}
