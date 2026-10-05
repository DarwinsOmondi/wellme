package com.example.wellme.presentation.merchant

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.theme.PrimaryBlue
import com.example.wellme.theme.PrimaryBlueLight
import com.example.wellme.theme.WellMeTheme
import java.util.Locale

@Composable
fun MerchantInventoryScreen(
    viewModel: MerchantInventoryViewModel,
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.inventory.collectAsState()
    val totalItems by viewModel.totalItemsCount.collectAsState()
    val lowStock by viewModel.lowStockCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    MerchantInventoryContent(
        inventory = inventory,
        totalItems = totalItems,
        lowStock = lowStock,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategoryChange = viewModel::onCategoryChange,
        onUpdateStock = viewModel::updateStock,
        onProfileClick = onProfileClick,
        modifier = modifier
    )
}

@Composable
fun MerchantInventoryContent(
    inventory: List<MerchantItem>,
    totalItems: Int,
    lowStock: Int,
    searchQuery: String,
    selectedCategory: String,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onUpdateStock: (String, Int) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All Items", "Food", "Drinks", "Merch")
    var itemToEdit by remember { mutableStateOf<MerchantItem?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            MerchantInventoryHeader(onProfileClick = onProfileClick)

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Search & Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search inventory...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Category Pills
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(categories) { category ->
                        CategoryPill(
                            name = category,
                            isSelected = selectedCategory == category,
                            onClick = { onCategoryChange(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // KPI Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KpiCard(
                        title = "Total Items",
                        value = totalItems.toString().padStart(2, '0'),
                        backgroundColor = PrimaryBlueLight,
                        contentColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Low Stock",
                        value = lowStock.toString().padStart(2, '0'),
                        backgroundColor = Color(0xFFFFDDB3),
                        contentColor = Color(0xFF8B5000),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Stock Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Items List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Spacing for FAB/Nav
                ) {
                    if (inventory.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No items found", color = Color.Gray)
                            }
                        }
                    }
                    items(inventory) { item ->
                        InventoryItemCard(
                            item = item,
                            onEdit = { itemToEdit = item }
                        )
                    }
                }
            }
        }

        // Edit Stock Dialog
        itemToEdit?.let { item ->
            EditStockDialog(
                item = item,
                onDismiss = { itemToEdit = null },
                onConfirm = { newStock ->
                    onUpdateStock(item.id, newStock)
                    itemToEdit = null
                }
            )
        }
    }
}

@Composable
fun EditStockDialog(
    item: MerchantItem,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var stockText by remember { mutableStateOf(item.stockUnits.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Enter the current number of units available in your stall.")
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) stockText = it },
                    label = { Text("Stock Units") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newStock = stockText.toIntOrNull() ?: item.stockUnits
                    onConfirm(newStock)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Update")
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
fun MerchantInventoryHeader(onProfileClick: () -> Unit) {
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
                model = "https://ui-avatars.com/api/?name=Merchant&background=2563EB&color=fff",
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "Inventory",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = { /* Settings */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun CategoryPill(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryBlue else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = if (isSelected) Color.White else Color.DarkGray,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, color = contentColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = contentColor, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun InventoryItemCard(item: MerchantItem, onEdit: () -> Unit) {
    val isLowStock = item.stockUnits <= 5
    
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
                model = item.imageUrl ?: R.drawable.ic_restaurant,
                contentDescription = item.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
                    Text(
                        text = String.format(Locale.getDefault(), "KSh %,.2f", item.price),
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stock: ${item.stockUnits} units",
                        color = if (isLowStock) Color.Red else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (isLowStock) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (item.category) {
                                    "Food" -> PrimaryBlueLight
                                    "Drinks" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE3F2FD)
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = when (item.category) {
                                "Food" -> PrimaryBlue
                                "Drinks" -> Color(0xFFEF6C00)
                                else -> Color(0xFF1565C0)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MerchantInventoryPreview() {
    WellMeTheme {
        MerchantInventoryContent(
            inventory = listOf(
                MerchantItem("1", "Sukuma Wiki", 50.0, 24, "Food", "Fresh greens", null, "m1"),
                MerchantItem("2", "Nyanya", 20.0, 3, "Food", "Tomatoes", null, "m1")
            ),
            totalItems = 42,
            lowStock = 5,
            searchQuery = "",
            selectedCategory = "All Items",
            onSearchQueryChange = {},
            onCategoryChange = {},
            onUpdateStock = { _, _ -> },
            onProfileClick = {}
        )
    }
}
