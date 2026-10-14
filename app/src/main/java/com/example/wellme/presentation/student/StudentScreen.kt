package com.example.wellme.presentation.student

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.wellme.R
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType
import com.example.wellme.presentation.common.BottomSheetContent
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueDark
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme
import com.example.wellme.util.GuestSession
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onDiscoverClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
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
        onDeposit = { amount, phone -> viewModel.initiateDeposit(amount, phone) },
        onRefresh = { viewModel.refreshWallet() },
        onProfileClick = onProfileClick,
        onDiscoverClick = onDiscoverClick,
        onNotificationClick = onNotificationClick,
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
    onDeposit: (Double, String) -> Unit,
    onRefresh: () -> Unit,
    onProfileClick: () -> Unit = {},
    onDiscoverClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    var showQuickActionsDialog by remember { mutableStateOf(false) }
    var showAllTransactionsDialog by remember { mutableStateOf(false) }
    var showAuthRequiredDialog by remember { mutableStateOf(false) }

    val isGuest = GuestSession.isGuest

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
                .background(Color(0xFFF8F9FB))
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Header
            StudentHeader(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                initials = initials,
                avatarUrl = studentKyc?.avatarUrl,
                onProfileClick = onProfileClick,
                onNotificationClick = onNotificationClick
            )

            // Balance Card
            StudentBalanceCard(
                balanceKsh = balanceKsh,
                modifier = Modifier.padding(horizontal = 20.dp),
                onDepositClick = {
                    if (isGuest) showAuthRequiredDialog = true
                    else showDepositDialog = true
                },
                onDiscoverClick = onDiscoverClick,
                onQuickActionsClick = { showQuickActionsDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions / Activity Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                if (transactions.isNotEmpty()) {
                    TextButton(onClick = { showAllTransactionsDialog = true }) {
                        Text("See All", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent transactions",
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Discover campus vendors and order food or top up your wallet via M-Pesa.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    transactions.take(5).forEach { tx ->
                        TransactionItem(transaction = tx)
                    }
                }
            }
        }

        // Dialogs
        if (showDepositDialog) {
            DepositDialog(
                onDismiss = { showDepositDialog = false },
                onConfirm = { amount, phone ->
                    showDepositDialog = false
                    onDeposit(amount, phone)
                }
            )
        }

        if (showQuickActionsDialog) {
            WalletQuickActionsDialog(
                onDepositClick = {
                    showQuickActionsDialog = false
                    if (isGuest) {
                        showAuthRequiredDialog = true
                    } else {
                        showDepositDialog = true
                    }
                },
                onRefreshClick = {
                    showQuickActionsDialog = false
                    onRefresh()
                },
                onProfileClick = {
                    showQuickActionsDialog = false
                    onProfileClick()
                },
                onDismiss = { showQuickActionsDialog = false }
            )
        }

        if (showAllTransactionsDialog) {
            AllTransactionsDialog(
                transactions = transactions,
                onDismiss = { showAllTransactionsDialog = false }
            )
        }

        if (showAuthRequiredDialog) {
            AuthRequiredDialog(
                onDismiss = { showAuthRequiredDialog = false },
                onSignIn = {
                    showAuthRequiredDialog = false
                    onProfileClick()
                }
            )
        }

        // Deposit Status Bottom Sheet
        AnimatedVisibility(
            visible = sheetState !is SheetState.Idle,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                BottomSheetContent(
                    state = sheetState,
                    onDismiss = {}
                )
            }
        }
    }
}

@Composable
fun StudentHeader(
    modifier: Modifier = Modifier,
    initials: String = "JD",
    avatarUrl: String? = null,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onProfileClick() },
                shape = CircleShape,
                color = PrimaryBlueLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Student Ecosystem",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onNotificationClick() },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = "Notifications",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudentBalanceCard(
    balanceKsh: Double,
    modifier: Modifier = Modifier,
    onDepositClick: () -> Unit,
    onDiscoverClick: () -> Unit,
    onQuickActionsClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Surface(
                    onClick = onQuickActionsClick,
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Actions",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = String.format(Locale.getDefault(), "KSh %,.2f", balanceKsh),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDiscoverClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_store),
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Discover Vendors",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onDepositClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.5.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deposit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val timeString = dateFormat.format(Date(transaction.timestamp))
    val isTopUp = transaction.type == TransactionType.STIPEND || transaction.merchantId == "mpesa"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (isTopUp) PrimaryBlueLight else Color(0xFFE8F5E9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isTopUp) Icons.Default.AccountBalanceWallet else Icons.Default.Store,
                        contentDescription = null,
                        tint = if (isTopUp) PrimaryBlue else Color(0xFF2E7D32),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isTopUp) "M-Pesa Wallet Top Up" else "Vendor Checkout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1C1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeString,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = String.format(Locale.getDefault(), "%sKSh %,.2f", if (isTopUp) "+" else "-", transaction.amountInCents / 100.0),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = if (isTopUp) PrimaryBlue else Color(0xFF2E7D32)
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Amount (KSh)") },
                    placeholder = { Text("1000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("M-Pesa Phone Number") },
                    placeholder = { Text("254712345678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && phone.isNotBlank()) {
                        onConfirm(amt, phone.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Proceed to Prompt", fontWeight = FontWeight.Bold)
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
fun WalletQuickActionsDialog(
    onDepositClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wallet Actions", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDepositClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Deposit via M-Pesa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Top up student wallet instantly",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRefreshClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = null,
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Refresh Balance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                "Sync latest transactions",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfileClick() },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_account_circle),
                            contentDescription = null,
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "View Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                "Manage account & security",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AllTransactionsDialog(
    transactions: List<Transaction>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Transactions", fontWeight = FontWeight.Bold) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            TransactionItem(transaction = tx)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AuthRequiredDialog(
    onDismiss: () -> Unit,
    onSignIn: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Authentication Required", fontWeight = FontWeight.Bold) },
        text = { Text("Please sign in or create an account to access student wallet deposits and payments.") },
        confirmButton = {
            Button(onClick = onSignIn, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                Text("Sign In", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StudentScreenPreview() {
    WellMeTheme {
        StudentContent(
            wallet = StudentWallet(
                studentId = "student_123",
                balanceInCents = 240000L
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
            onDeposit = { _, _ -> },
            onRefresh = {}
        )
    }
}
