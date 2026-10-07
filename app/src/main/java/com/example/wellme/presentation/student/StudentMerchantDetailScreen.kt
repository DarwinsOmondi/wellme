package com.example.wellme.presentation.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.theme.WellMeTheme
import java.util.Locale
import kotlin.math.roundToInt

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

    StudentMerchantDetailContent(
        merchant = merchant,
        inventory = inventory,
        cart = cart,
        total = total,
        studentId = viewModel.studentId,
        onBack = onBack,
        onClearCart = { viewModel.clearCart() },
        onAddToCart = { viewModel.addToCart(it) },
        onRemoveFromCart = { viewModel.removeFromCart(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMerchantDetailContent(
    merchant: MerchantProfile?,
    inventory: List<MerchantItem>,
    cart: List<MerchantItem>,
    total: Double,
    studentId: String,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
    onAddToCart: (MerchantItem) -> Unit,
    onRemoveFromCart: (MerchantItem) -> Unit
) {
    var showQrDialog by remember { mutableStateOf(false) }

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

            // Bottom Cart / Swipe Area
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    ) {
                        // Swipe to generate QR UI
                        SwipeToLeftQr(
                            total = total,
                            onSwipeComplete = { showQrDialog = true }
                        )
                    }
                }
            }
        }
        
        if (showQrDialog) {
            PaymentQrDialog(
                studentId = studentId,
                amount = total,
                merchantId = merchant?.merchantId ?: "",
                onDismiss = { showQrDialog = false }
            )
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

@Composable
fun SwipeToLeftQr(
    total: Double,
    onSwipeComplete: () -> Unit
) {
    var dragAmount by remember { mutableStateOf(0f) }
    var maxDragWidth by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val thumbSize = 60.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    var isTriggered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp)
            .onSizeChanged {
                maxDragWidth = (it.width.toFloat() - thumbSizePx - with(density) { 24.dp.toPx() }).coerceAtLeast(0f)
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        // Total Indicator
        Row(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total:",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format(Locale.getDefault(), "KSh %,.2f", total),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Hint text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<< SWIPE TO GENERATE QR",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }

        // Draggable Handle (Right Side)
        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset { IntOffset((-dragAmount).roundToInt(), 0) }
                .size(thumbSize)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newValue = dragAmount - delta
                        dragAmount = newValue.coerceIn(0f, maxDragWidth)
                    },
                    onDragStopped = {
                        if (dragAmount >= maxDragWidth * 0.75f && !isTriggered) {
                            isTriggered = true
                            dragAmount = maxDragWidth
                            onSwipeComplete()
                        } else {
                            dragAmount = 0f
                        }
                    }
                )
                .clickable { onSwipeComplete() },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_qr_code),
                    contentDescription = null, 
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentQrDialog(
    studentId: String,
    amount: Double,
    merchantId: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Payment Ready",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Show this code to the vendor to process your payment of KSh ${String.format("%.2f", amount)}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // QR Placeholder
                Surface(
                    modifier = Modifier.size(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        com.example.wellme.presentation.common.QrCodeWidget(seed = (studentId + amount).hashCode())
                    }
                }
                
                Text(
                    "Vendor will scan this to prompt you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Done")
            }
        }
    )
}
