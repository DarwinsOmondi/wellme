package com.example.wellme.presentation.student

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.PendingPayment
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.WalletRepository
import com.example.wellme.domain.usecase.InitiateStkPushUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed interface PaymentPayState {
    object Idle : PaymentPayState
    object Processing : PaymentPayState
    data class Pending(val checkoutRequestId: String, val message: String) : PaymentPayState
    data class Success(val message: String) : PaymentPayState
    data class Error(val message: String) : PaymentPayState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentDiscoverViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val inventoryRepository: MerchantInventoryRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
    private val initiateStkPushUseCase: InitiateStkPushUseCase,
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

    private var pendingJob: kotlinx.coroutines.Job? = null

    fun observePendingPayment(checkoutRequestId: String) {
        pendingJob?.cancel()
        pendingJob = viewModelScope.launch {
            walletRepository.observePendingPayment(checkoutRequestId)
                .collect { pending ->
                    Log.d(TAG, "Observed payment status: ${pending.status}")
                    if (pending.status.equals("SUCCESS", ignoreCase = true)) {
                        _paymentState.value = PaymentPayState.Success("Payment confirmed successfully! Thank you.")
                        _cart.value = emptyList()
                    } else if (pending.status.equals("FAILED", ignoreCase = true)) {
                        _paymentState.value = PaymentPayState.Error("M-Pesa payment failed or was cancelled.")
                    }
                }
        }
    }

    fun payMpesaExpress(amountInKsh: Double, phoneNumber: String) {
        val amountInCents = (amountInKsh * 100).toLong()
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

                result.onSuccess { response ->
                    Log.d(TAG, "STK Push successful. CheckoutRequestId: ${response.checkoutRequestId}")
                    try {
                        val pendingPayment = PendingPayment(
                            checkoutRequestId = response.checkoutRequestId,
                            studentId = studentId,
                            amountInCents = amountInCents,
                            status = "PENDING",
                            timestamp = System.currentTimeMillis()
                        )
                        walletRepository.logPendingPayment(pendingPayment)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to log pending payment", e)
                    }

                    _paymentState.value = PaymentPayState.Pending(
                        response.checkoutRequestId,
                        "STK Push prompt sent to $phoneNumber. Please enter your M-Pesa PIN on your phone."
                    )
                    observePendingPayment(response.checkoutRequestId)
                }.onFailure {
                    _paymentState.value = PaymentPayState.Error(it.message ?: "STK Push failed")
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentPayState.Error(e.message ?: "Error initiating payment")
            }
        }
    }

    fun clearPaymentState() {
        pendingJob?.cancel()
        _paymentState.value = PaymentPayState.Idle
    }
}
