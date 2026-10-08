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
import com.example.wellme.domain.usecase.InitiateStkPushUseCase
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
    private val initiateStkPushUseCase: InitiateStkPushUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
        Log.d("StudentDiscoverVM", "Inventory flow requested for merchant ID: $id")
        if (id == null) {
            Log.d("StudentDiscoverVM", "Merchant ID is null, emitting empty inventory")
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

    fun payMpesaExpress(amountInKsh: Double, phoneNumber: String) {
        if (amountInKsh <= 0) return
        viewModelScope.launch {
            _paymentState.value = PaymentPayState.Processing
            try {
                val result = initiateStkPushUseCase(
                    consumerKey = com.example.wellme.BuildConfig.MPESA_CONSUMER_KEY,
                    consumerSecret = com.example.wellme.BuildConfig.MPESA_CONSUMER_SECRET,
                    businessShortCode = "174379",
                    passkey = com.example.wellme.BuildConfig.MPESA_PASSKEY,
                    amount = amountInKsh.toInt().toString(),
                    phoneNumber = phoneNumber,
                    callbackUrl = com.example.wellme.BuildConfig.MPESA_CALLBACK_URL,
                    accountReference = "Vendor Payment",
                    transactionDesc = "Checkout Payment"
                )

                result.onSuccess {
                    _paymentState.value = PaymentPayState.Success("M-Pesa Express prompt sent to $phoneNumber. Please enter your PIN on your phone to complete payment.")
                    _cart.value = emptyList()
                }.onFailure {
                    _paymentState.value = PaymentPayState.Error(it.message ?: "STK Push failed")
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentPayState.Error(e.message ?: "Error initiating payment")
            }
        }
    }

    fun clearPaymentState() {
        _paymentState.value = PaymentPayState.Idle
    }
}
