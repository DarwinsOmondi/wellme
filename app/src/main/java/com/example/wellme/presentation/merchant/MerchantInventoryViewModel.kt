package com.example.wellme.presentation.merchant

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.MerchantInventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MerchantInventoryViewModel @Inject constructor(
    private val inventoryRepository: MerchantInventoryRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val merchantId = authRepository.getCurrentUser()?.id ?: ""

    // --- POS (Collect Payment) State ---
    private val _cart = MutableStateFlow<List<MerchantItem>>(emptyList())
    val cart = _cart.asStateFlow()

    val totalPrice = _cart.map { items ->
        items.sumOf { it.price }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(item: MerchantItem) {
        _cart.value += item
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // --- Inventory State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All Items")
    val selectedCategory = _selectedCategory.asStateFlow()

    val inventory = inventoryRepository.getInventory(merchantId)
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
        }
        .combine(_selectedCategory) { list, category ->
            if (category == "All Items") list else list.filter { it.category == category }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // KPI Metrics
    val totalItemsCount = inventory.map { it.sumOf { item -> item.stockUnits } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowStockCount = inventory.map { it.count { item -> item.stockUnits <= 5 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    // --- Actions ---
    fun addItem(item: MerchantItem) {
        viewModelScope.launch {
            inventoryRepository.upsertItem(item.copy(merchantId = merchantId))
        }
    }

    fun updateStock(itemId: String, newStock: Int) {
        viewModelScope.launch {
            inventoryRepository.updateStock(itemId, newStock)
        }
    }

    // --- QR Code Dialog State ---
    private val _qrDialogAmount = MutableStateFlow<Double?>(null)
    val qrDialogAmount = _qrDialogAmount.asStateFlow()

    fun generateQr(total: Double) {
        if (total > 0) {
            _qrDialogAmount.value = total
        }
    }

    fun dismissQrDialog() {
        _qrDialogAmount.value = null
        clearCart()
    }
}
