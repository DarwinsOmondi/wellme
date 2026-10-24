package com.example.wellme.presentation.merchant

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MerchantInventoryViewModel @Inject constructor(
    private val inventoryRepository: MerchantInventoryRepository,
    private val kycRepository: KycRepository,
    private val storage: Storage,
    authRepository: AuthRepository
) : ViewModel() {

    private val merchantId = authRepository.getCurrentUser()?.id ?: ""

    private val _merchantAvatarUrl = MutableStateFlow<String?>(null)
    val merchantAvatarUrl: StateFlow<String?> = _merchantAvatarUrl.asStateFlow()

    init {
        loadMerchantAvatar()
    }

    private fun loadMerchantAvatar() {
        if (merchantId.isBlank()) return
        viewModelScope.launch {
            try {
                val kyc = kycRepository.getMerchantKyc(merchantId).getOrNull()
                _merchantAvatarUrl.value = kyc?.avatarUrl
            } catch (e: Exception) {
                android.util.Log.e("MerchantInventoryVM", "Error loading merchant avatar", e)
            }
        }
    }

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
    private val _refreshTrigger = MutableStateFlow(0)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _refreshTrigger.value += 1
            } catch (e: Throwable) {
                android.util.Log.e("MerchantInventoryVM", "Error refreshing inventory", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All Items")
    val selectedCategory = _selectedCategory.asStateFlow()

    val inventory = _refreshTrigger.flatMapLatest {
        inventoryRepository.getInventory(merchantId)
    }.combine(_searchQuery) { list, query ->
        if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
    }.combine(_selectedCategory) { list, category ->
        if (category == "All Items") list else list.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    fun addItem(item: MerchantItem, onComplete: () -> Unit = {}) {
        val activeMerchantId = merchantId.ifBlank { "m1" }
        viewModelScope.launch {
            try {
                inventoryRepository.upsertItem(item.copy(merchantId = activeMerchantId))
                _refreshTrigger.value += 1
            } catch (e: Throwable) {
                android.util.Log.e("MerchantInventoryVM", "Error adding item", e)
            } finally {
                onComplete()
            }
        }
    }

    fun addItem(item: MerchantItem, imageUri: Uri?, context: Context, onComplete: () -> Unit = {}) {
        val activeMerchantId = merchantId.ifBlank { "m1" }
        viewModelScope.launch {
            try {
                var finalImageUrl = item.imageUrl
                if (imageUri != null) {
                    val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val mimeType = context.contentResolver.getType(imageUri)
                        val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "jpg"
                        val path = "inventory/$activeMerchantId/${UUID.randomUUID()}.$extension"
                        val bucket = storage.from("user-profiles")
                        bucket.upload(path, bytes) { upsert = true }
                        finalImageUrl = bucket.publicUrl(path)
                        android.util.Log.d("MerchantInventoryVM", "Item image uploaded successfully: $finalImageUrl")
                    } else {
                        android.util.Log.e("MerchantInventoryVM", "Failed to read bytes from imageUri: $imageUri")
                    }
                }
                inventoryRepository.upsertItem(item.copy(merchantId = activeMerchantId, imageUrl = finalImageUrl))
                _refreshTrigger.value += 1
            } catch (e: Throwable) {
                android.util.Log.e("MerchantInventoryVM", "Error adding item with image", e)
            } finally {
                onComplete()
            }
        }
    }

    fun updateStock(itemId: String, newStock: Int) {
        viewModelScope.launch {
            try {
                inventoryRepository.updateStock(itemId, newStock)
            } catch (e: Throwable) {
                android.util.Log.e("MerchantInventoryVM", "Error updating stock", e)
            }
        }
    }

    fun toggleAvailability(item: MerchantItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                inventoryRepository.setItemAvailability(item.id, !item.isAvailable)
                _refreshTrigger.value += 1
            } catch (e: Throwable) {
                android.util.Log.e("MerchantInventoryVM", "Error toggling availability", e)
            } finally {
                onComplete()
            }
        }
    }

    fun discontinueItem(item: MerchantItem, onComplete: () -> Unit = {}) {
        toggleAvailability(item, onComplete)
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
