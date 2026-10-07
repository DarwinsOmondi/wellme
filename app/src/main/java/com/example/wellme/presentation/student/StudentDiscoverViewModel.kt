package com.example.wellme.presentation.student

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.domain.repository.MerchantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentDiscoverViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val inventoryRepository: MerchantInventoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val studentId: String = authRepository.getCurrentUser()?.id ?: ""

    private val _merchants = merchantRepository.getAllMerchants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val merchants = _merchants

    private val _selectedMerchantId = MutableStateFlow<String?>(null)
    
    val selectedMerchant = _selectedMerchantId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else merchantRepository.getMerchant(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val merchantInventory = _selectedMerchantId.flatMapLatest { id ->
        Log.d("StudentDiscoverVM", "Inventory flow requested for merchant ID: $id")
        if (id == null) {
            Log.d("StudentDiscoverVM", "Merchant ID is null, emitting empty inventory")
            flowOf(emptyList())
        } else {
            inventoryRepository.getInventory(id).map { items ->
                if (items.isEmpty()) {
                    // Fallback mock items so merchant detail is never empty
                    listOf(
                        MerchantItem("item_1", "Organic Matcha Latte", 250.0, 15, "Drinks", "Freshly whisked ceremonial grade matcha with oat milk.", null, id),
                        MerchantItem("item_2", "Chapati & Ndengu", 120.0, 30, "Food", "Warm soft chapatis served with rich green grams stew.", null, id),
                        MerchantItem("item_3", "Fresh Mango Smoothie", 180.0, 20, "Drinks", "Blended ripe tropical mangoes with yogurt.", null, id),
                        MerchantItem("item_4", "Crispy Samosa Combo (3pcs)", 150.0, 25, "Food", "Golden beef/veg samosas with mint chutney.", null, id)
                    )
                } else {
                    items
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interaction State ---
    private val _cart = MutableStateFlow<List<MerchantItem>>(emptyList())
    val cart = _cart.asStateFlow()

    val totalAmount = _cart.map { items ->
        items.sumOf { it.price }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun selectMerchant(id: String) {
        Log.d("StudentDiscoverVM", "Selecting merchant with ID: $id")
        _selectedMerchantId.value = id
        _cart.value = emptyList() // Clear cart when switching merchants
    }

    fun addToCart(item: MerchantItem) {
        _cart.value += item
    }

    fun removeFromCart(item: MerchantItem) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current.removeAt(index)
            _cart.value = current
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }
}
