package com.example.wellme.presentation.student

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.usecase.ProcessPaymentUseCase
import com.example.wellme.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed interface PaymentPayState {
    object Idle : PaymentPayState
    object Processing : PaymentPayState
    data class Success(val message: String) : PaymentPayState
    data class Error(val message: String) : PaymentPayState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentDiscoverViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val inventoryRepository: MerchantInventoryRepository,
    private val authRepository: AuthRepository,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val TAG = "StudentDiscoverVM"
    private val initialMerchantId: String? = savedStateHandle["merchantId"]

    val studentId: String = authRepository.getCurrentUser()?.id ?: ""

    private val _merchants = merchantRepository.getAllMerchants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val merchants = _merchants

    private val _selectedMerchantId = MutableStateFlow<String?>(initialMerchantId)
    
    val selectedMerchant = _selectedMerchantId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else merchantRepository.getMerchant(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val merchantInventory = _selectedMerchantId.flatMapLatest { id ->
        Log.d(TAG, "Inventory flow requested for merchant ID: $id")
        if (id == null) {
            Log.d(TAG, "Merchant ID is null, emitting empty inventory")
            flowOf(emptyList())
        } else {
            inventoryRepository.getInventory(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interaction State ---
    private val _cart = MutableStateFlow<List<MerchantItem>>(emptyList())
    val cart = _cart.asStateFlow()

    val totalAmount = _cart.map { items ->
        items.sumOf { it.price }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _paymentState = MutableStateFlow<PaymentPayState>(PaymentPayState.Idle)
    val paymentState: StateFlow<PaymentPayState> = _paymentState.asStateFlow()

    fun selectMerchant(id: String) {
        Log.d(TAG, "Selecting merchant with ID: $id")
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

    fun checkoutCart(merchantId: String, originalAmountInCents: Long) {
        if (originalAmountInCents <= 0) return
        viewModelScope.launch {
            _paymentState.value = PaymentPayState.Processing
            try {
                val transaction = processPaymentUseCase(
                    studentId = studentId,
                    merchantId = merchantId,
                    originalAmountInCents = originalAmountInCents
                )
                Log.d(TAG, "Checkout successful. Net debited: ${transaction.amountInCents}, Discount applied: ${transaction.discountAppliedInCents}")
                _paymentState.value = PaymentPayState.Success(
                    "Payment of KSh ${transaction.amountInCents / 100.0} completed successfully! (Saved KSh ${transaction.discountAppliedInCents / 100.0} with community pool discount)."
                )
                _cart.value = emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Checkout failed", e)
                _paymentState.value = PaymentPayState.Error(ErrorMapper.getUserFriendlyMessage(e))
            }
        }
    }

    fun clearPaymentState() {
        _paymentState.value = PaymentPayState.Idle
    }
}
