package com.example.wellme.presentation.merchant

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.wellme.R
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.Transaction
import com.example.wellme.presentation.merchant.MerchantDashboard
import com.example.wellme.presentation.merchant.RequestCapitalTab

@Composable
fun MerchantScreen(
    viewModel: MerchantViewModel,
    inventoryViewModel: MerchantInventoryViewModel = hiltViewModel(),
    onProfileClick: () -> Unit = {},
    onNavigateToAddItem: () -> Unit = {}
) {
    val merchant by viewModel.merchant.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val progress by viewModel.poolProgress.collectAsState()
    
    var activeTab by remember { mutableStateOf(0) } // 0: Inventory, 1: Payments, 2: History, 3: Impact

    Scaffold(
        containerColor = Color(0xFFF8F9FB),
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                MerchantBottomNavigation(
                    selectedTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = {
                        android.util.Log.d("MerchantScreen", "FAB Clicked")
                        onNavigateToAddItem()
                    },
                    containerColor = Color(0xFF006D3E),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Crossfade(targetState = activeTab, label = "TabSwitch") { tab ->
                when (tab) {
                    0 -> MerchantInventoryScreen(
                        viewModel = inventoryViewModel,
                        onProfileClick = onProfileClick,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> MerchantPOSScreen(
                        viewModel = inventoryViewModel,
                        onProfileClick = onProfileClick,
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> MerchantDashboard(
                        merchant = merchant,
                        progress = progress,
                        transactions = transactions,
                        modifier = Modifier.fillMaxSize()
                    )
                    3 -> RequestCapitalTab(
                        amountInput = viewModel.amountInput,
                        onAmountInputChange = viewModel::onAmountInputChange,
                        selectedYield = viewModel.selectedYield,
                        onSelectedYieldChange = viewModel::onSelectedYieldChange,
                        loanState = viewModel.loanState,
                        onSubmit = { viewModel.submitCapitalRequest() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MerchantBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp, // Elevation handled by Surface wrapper
        modifier = Modifier.height(80.dp)
    ) {
        val items = listOf(
            Triple("Inventory", Icons.Default.Inventory, 0),
            Triple("Payments", Icons.Default.QrCodeScanner, 1),
            Triple("History", Icons.AutoMirrored.Filled.ReceiptLong, 2),
            Triple("Impact", Icons.Default.TrendingUp, 3)
        )

        items.forEach { (label, icon, index) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFF006D3E) else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF006D3E),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFFE8F5E9)
                )
            )
        }
    }
}
