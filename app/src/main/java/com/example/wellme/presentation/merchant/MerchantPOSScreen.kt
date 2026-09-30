package com.example.wellme.presentation.merchant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.theme.WellMeTheme
import java.util.Locale
import androidx.core.net.toUri

@Composable
fun MerchantPOSScreen(
    viewModel: MerchantInventoryViewModel,
    merchantViewModel: MerchantViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.totalPrice.collectAsState()
    val qrDialogAmount by viewModel.qrDialogAmount.collectAsState()
    
    var isScanningCustomer by remember { mutableStateOf(false) }

    qrDialogAmount?.let { amount ->
        MerchantQrPaymentDialog(
            amount = amount,
            onDismiss = { viewModel.dismissQrDialog() }
        )
    }

    if (isScanningCustomer) {
        com.example.wellme.presentation.common.ScannerOverlay(
            onClose = { isScanningCustomer = false },
            onScan = { data, _ -> // Ignoring simulated long for now
                try {
                    val uri = data.toUri()
                    // Format: wellme_pay://student_id?amount=100
                    val studentId = uri.host ?: ""
                    val amount = uri.getQueryParameter("amount")?.toDoubleOrNull() ?: 0.0
                    if (studentId.isNotEmpty() && amount > 0) {
                        merchantViewModel.processCustomerPayment(studentId, (amount * 100).toLong())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MerchantPOS", "Invalid QR data: $data")
                }
                isScanningCustomer = false
            }
        )
    } else {
        MerchantPOSContent(
            inventory = inventory,
            cart = cart,
            total = total,
            onAddToCart = viewModel::addToCart,
            onClearCart = viewModel::clearCart,
            onGenerateQr = viewModel::generateQr,
            onProfileClick = onProfileClick,
            onScanCustomer = { isScanningCustomer = true },
            modifier = modifier
        )
    }
}

@Composable
fun MerchantPOSContent(
    inventory: List<MerchantItem>,
    cart: List<MerchantItem>,
    total: Double,
    onAddToCart: (MerchantItem) -> Unit,
    onClearCart: () -> Unit,
    onGenerateQr: (Double) -> Unit,
    onProfileClick: () -> Unit,
    onScanCustomer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
            .statusBarsPadding()
    ) {
        // Header
        MerchantPOSHeader(onProfileClick = onProfileClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Inventory Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Button(
                    onClick = onScanCustomer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_qr_code),
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Customer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Add Horizontal List
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(inventory.filter { !it.name.contains("Avocado", ignoreCase = true) }) { item ->
                    QuickAddItemCard(item = item, onAdd = { onAddToCart(item) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Featured Item
            inventory.find { it.name.contains("Avocado", ignoreCase = true) }?.let { featured ->
                FeaturedItemCard(item = featured, onAdd = { onAddToCart(featured) })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Current Order Area
            OrderSummarySection(
                cart = cart,
                total = total,
                onClear = onClearCart
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Swipe Action
        SwipeToGenerateQr(
            onSwipeComplete = { onGenerateQr(total) },
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun MerchantPOSHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() },
            color = Color.LightGray
        ) {
            AsyncImage(
                model = "https://ui-avatars.com/api/?name=Merchant&background=006D3E&color=fff",
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "Collect Payment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF006D3E)
        )

        IconButton(onClick = onProfileClick) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun MerchantQrPaymentDialog(
    amount: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Customer Checkout QR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF006D3E)
                )
                Text(
                    text = String.format(Locale.getDefault(), "KSh %,.2f", amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = Color(0xFF1A1C1E),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        com.example.wellme.presentation.common.QrCodeWidget(
                            seed = amount.toInt(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ask student to scan this QR code using their WellMe app to process payment.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D3E))
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun QuickAddItemCard(item: MerchantItem, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onAdd() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl ?: R.drawable.ic_restaurant,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    maxLines = 1
                )
                Text(
                    text = String.format(Locale.getDefault(), "KSh %,.0f", item.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF006D3E),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun FeaturedItemCard(item: MerchantItem, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl ?: R.drawable.ic_restaurant,
                contentDescription = item.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = item.description ?: "Organic selection",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
                Text(
                    text = String.format(Locale.getDefault(), "KSh %,.0f", item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF006D3E),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun OrderSummarySection(
    cart: List<MerchantItem>,
    total: Double,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF1F4F9)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT ORDER",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = onClear) {
                    Text("Clear", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cart.isEmpty()) {
                Text(
                    text = "Tap items to add to order...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                cart.groupBy { it.id }.values.forEach { group ->
                    val item = group.first()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${group.size}x ${item.name}", fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
                        Text(String.format(Locale.getDefault(), "KSh %,.0f", item.price * group.size), color = Color(0xFF1A1C1E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                Text(
                    text = String.format(Locale.getDefault(), "KSh %,.2f", total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF006D3E)
                )
            }
        }
    }
}

@Composable
fun SwipeToGenerateQr(
    onSwipeComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFE8F0FF)
    ) {
        Box(contentAlignment = Alignment.CenterStart) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SWIPE TO GENERATE QR",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00391C),
                    letterSpacing = 1.sp
                )
            }
            
            Surface(
                modifier = Modifier
                    .padding(4.dp)
                    .size(56.dp)
                    .clickable { onSwipeComplete() },
                shape = CircleShape,
                color = Color(0xFF006D3E),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MerchantPOSPreview() {
    WellMeTheme {
        val mockItems = listOf(
            MerchantItem("1", "Sukuma Wiki", 50.0, 24, "Food", "Fresh greens", null, "m1"),
            MerchantItem("2", "Nyanya", 20.0, 10, "Food", "Tomatoes", null, "m1"),
            MerchantItem("3", "Parachichi", 40.0, 15, "Food", "Avocado", null, "m1")
        )
        MerchantPOSContent(
            inventory = mockItems,
            cart = listOf(mockItems[0], mockItems[0], mockItems[1]),
            total = 120.0,
            onAddToCart = {},
            onClearCart = {},
            onGenerateQr = {},
            onProfileClick = {}
        )
    }
}
