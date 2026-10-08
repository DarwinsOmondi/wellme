package com.example.wellme.presentation.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueDark
import com.example.wellme.theme.WellMeTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMerchantDetailScreen(
    viewModel: StudentDiscoverViewModel,
    onBack: () -> Unit
) {
    val merchant by viewModel.selectedMerchant.collectAsState()
    val inventory by viewModel.merchantInventory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.totalAmount.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()

    StudentMerchantDetailContent(
        merchant = merchant,
        inventory = inventory,
        cart = cart,
        total = total,
        paymentState = paymentState,
        studentId = viewModel.studentId,
        onBack = onBack,
        onClearCart = { viewModel.clearCart() },
        onAddToCart = { viewModel.addToCart(it) },
        onRemoveFromCart = { viewModel.removeFromCart(it) },
        onPayMpesaExpress = { amount, phone -> viewModel.payMpesaExpress(amount, phone) },
        onClearPaymentState = { viewModel.clearPaymentState() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMerchantDetailContent(
    merchant: MerchantProfile?,
    inventory: List<MerchantItem>,
    cart: List<MerchantItem>,
    total: Double,
    paymentState: PaymentPayState,
    studentId: String,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
    onAddToCart: (MerchantItem) -> Unit,
    onRemoveFromCart: (MerchantItem) -> Unit,
    onPayMpesaExpress: (Double, String) -> Unit,
    onClearPaymentState: () -> Unit
) {
    var showMpesaExpressSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(merchant?.businessName ?: "Vendor Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cart.isNotEmpty()) {
                        IconButton(onClick = onClearCart) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = Color.Red)
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FB)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Merchant Header Info
                merchant?.let { m ->
                    MerchantSummaryBar(m)
                }

                // Inventory List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Current Stock & Menu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(inventory) { item ->
                        StudentInventoryItemCard(
                            item = item,
                            quantityInCart = cart.count { it.id == item.id },
                            onAdd = { onAddToCart(item) },
                            onRemove = { onRemoveFromCart(item) }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }

            // Bottom Cart / Pay Area
            if (cart.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Amount", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = String.format(Locale.getDefault(), "KSh %,.2f", total),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { showMpesaExpressSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Pay via M-Pesa", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
        
        if (showMpesaExpressSheet || paymentState !is PaymentPayState.Idle) {
            MpesaExpressPayBottomSheet(
                merchantName = merchant?.businessName ?: "Verified Vendor",
                amountKsh = total,
                paymentState = paymentState,
                onConfirmPay = { phone ->
                    onPayMpesaExpress(total, phone)
                },
                onDismiss = {
                    showMpesaExpressSheet = false
                    onClearPaymentState()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MpesaExpressPayBottomSheet(
    merchantName: String,
    amountKsh: Double,
    paymentState: PaymentPayState,
    onConfirmPay: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("254712345678") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "M-Pesa Express Checkout",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (paymentState) {
                is PaymentPayState.Idle -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF8F9FA),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vendor / Merchant", color = Color.Gray, fontSize = 14.sp)
                                Text(merchantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Merchant Till", color = Color.Gray, fontSize = 14.sp)
                                Text("Till 174379", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryBlue)
                            }
                            HorizontalDivider(color = Color(0xFFE0E0E0))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Fixed Payable Amount", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    text = String.format(Locale.getDefault(), "KSh %,.2f", amountKsh),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number to Prompt") },
                        placeholder = { Text("e.g. 254712345678") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { if (phoneNumber.isNotBlank()) onConfirmPay(phoneNumber) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "Send M-Pesa STK Prompt",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is PaymentPayState.Processing -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Sending M-Pesa STK Prompt...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please check your phone and enter your M-Pesa PIN.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                is PaymentPayState.Success -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_verified),
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "STK Prompt Sent!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = paymentState.message,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Done")
                    }
                }

                is PaymentPayState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Payment Request Failed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Red
                    )
                    Text(
                        text = paymentState.message,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Gray
                    )
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun MerchantSummaryBar(merchant: MerchantProfile) {
    var showFundDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${(merchant.discountTier * 100).toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = merchant.businessName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "WellMe Registered Vendor • Tap Fund to Support",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Button(
                onClick = { showFundDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Fund", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showFundDialog) {
        AlertDialog(
            onDismissRequest = { showFundDialog = false },
            title = { Text("Support ${merchant.businessName}", fontWeight = FontWeight.Bold) },
            text = {
                Text("Contribute to this merchant's community pool to help them expand stock and earn community investment rewards.")
            },
            confirmButton = {
                Button(
                    onClick = { showFundDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Donate via M-Pesa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFundDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun StudentInventoryItemCard(
    item: MerchantItem,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl?.takeIf { it.isNotBlank() } ?: R.drawable.ic_restaurant,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_restaurant),
                error = painterResource(id = R.drawable.ic_restaurant)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = String.format(Locale.getDefault(), "KSh %,.2f", item.price),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "In stock: ${item.stockUnits} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.stockUnits < 5) Color.Red else Color.Gray
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (quantityInCart > 0) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Gray)
                    }
                    Text(text = quantityInCart.toString(), fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
