package com.example.wellme.presentation.student

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.BuildConfig
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.PendingPayment
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.WalletRepository
import com.example.wellme.domain.usecase.InitiateStkPushUseCase
import com.example.wellme.domain.usecase.ProcessPaymentUseCase
import com.example.wellme.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.math.roundToLong

sealed interface SheetState {
    object Idle : SheetState
    data class Scanned(val merchant: MerchantProfile, val originalAmountInCents: Long, val discountAppliedInCents: Long, val netAmountInCents: Long) : SheetState
    object Processing : SheetState
    data class Success(val transaction: Transaction) : SheetState
    data class Error(val message: String) : SheetState
}

sealed interface CustDepositState {
    object Idle : CustDepositState
    object Processing : CustDepositState
    data class Success(val message: String) : CustDepositState
    data class Error(val message: String) : CustDepositState
}

data class MerchantFundingRequest(
    val requestId: String,
    val merchantName: String,
    val amountRequestedKsh: Double,
    val yieldPercentage: Int,
    val mpesaTillNumber: String,
    val timestamp: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val merchantRepository: MerchantRepository,
    private val authRepository: AuthRepository,
    private val kycRepository: KycRepository,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val initiateStkPushUseCase: InitiateStkPushUseCase
) : ViewModel() {

    private val TAG = "StudentViewModel"

    val studentId: String = authRepository.getCurrentUser()?.id ?: ""

    private val _refreshTrigger = MutableStateFlow(0)
    private val _observeRealtime = MutableStateFlow(false)

    val wallet: StateFlow<StudentWallet?> = combine(_refreshTrigger, _observeRealtime) { _, observe -> observe }
        .flatMapLatest { observe ->
            walletRepository.getWallet(studentId, observe)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<Transaction>> = walletRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sheetState = MutableStateFlow<SheetState>(SheetState.Idle)
    val sheetState: StateFlow<SheetState> = _sheetState.asStateFlow()

    private val _depositState = MutableStateFlow<CustDepositState>(CustDepositState.Idle)
    val depositState: StateFlow<CustDepositState> = _depositState.asStateFlow()

    private val _merchantFundingRequests = MutableStateFlow<List<MerchantFundingRequest>>(
        listOf(
            MerchantFundingRequest(
                requestId = "req_1",
                merchantName = "Campus Cafe & Bakery",
                amountRequestedKsh = 25000.0,
                yieldPercentage = 15,
                mpesaTillNumber = "522123",
                timestamp = System.currentTimeMillis() - 3600000L
            ),
            MerchantFundingRequest(
                requestId = "req_2",
                merchantName = "Kibera Fresh Groceries",
                amountRequestedKsh = 50000.0,
                yieldPercentage = 20,
                mpesaTillNumber = "889944",
                timestamp = System.currentTimeMillis() - 86400000L
            )
        )
    )
    val merchantFundingRequests: StateFlow<List<MerchantFundingRequest>> = _merchantFundingRequests.asStateFlow()

    private val _studentKyc = MutableStateFlow<com.example.wellme.data.remote.model.StudentKyc?>(null)
    val studentKyc: StateFlow<com.example.wellme.data.remote.model.StudentKyc?> = _studentKyc.asStateFlow()

    init {
        Log.d(TAG, "Initializing StudentViewModel for studentId: $studentId")
        loadStudentKyc()
    }

    private fun loadStudentKyc() {
        if (studentId.isBlank()) return
        Log.d(TAG, "loadStudentKyc for studentId: $studentId")
        viewModelScope.launch {
            kycRepository.getStudentKyc(studentId).onSuccess { kyc ->
                Log.d(TAG, "Student KYC loaded: ${kyc?.studentName}")
                _studentKyc.value = kyc
            }.onFailure {
                Log.e(TAG, "Failed to load student KYC", it)
            }
        }
    }

    fun scanMerchant(merchantId: String, originalAmountInCents: Long) {
        Log.d(TAG, "scanMerchant: $merchantId, amount: $originalAmountInCents")
        viewModelScope.launch {
            _sheetState.value = SheetState.Processing
            val merchant = merchantRepository.getMerchant(merchantId).firstOrNull()
            if (merchant == null) {
                Log.e(TAG, "Merchant not found: $merchantId")
                _sheetState.value = SheetState.Error("Vendor '$merchantId' not registered in WellMe ecosystem.")
                return@launch
            }

            val discountApplied = (originalAmountInCents * merchant.discountTier).roundToLong()
            val netAmount = originalAmountInCents - discountApplied

            Log.d(TAG, "Merchant found: ${merchant.businessName}. Discount: $discountApplied, Net: $netAmount")

            _sheetState.value = SheetState.Scanned(
                merchant = merchant,
                originalAmountInCents = originalAmountInCents,
                discountAppliedInCents = discountApplied,
                netAmountInCents = netAmount
            )
        }
    }

    fun confirmPayment() {
        val currentState = _sheetState.value
        if (currentState !is SheetState.Scanned) return

        Log.d(TAG, "confirmPayment for merchant: ${currentState.merchant.merchantId}, amount: ${currentState.originalAmountInCents}")

        viewModelScope.launch {
            _sheetState.value = SheetState.Processing
            try {
                val transaction = processPaymentUseCase(
                    studentId = studentId,
                    merchantId = currentState.merchant.merchantId,
                    originalAmountInCents = currentState.originalAmountInCents
                )
                Log.d(TAG, "Payment successful. Syncing wallet...")
                refreshWallet() // Ensure UI shows the deduction
                _sheetState.value = SheetState.Success(transaction)
            } catch (e: Exception) {
                Log.e(TAG, "Payment failed", e)
                _sheetState.value = SheetState.Error(ErrorMapper.getUserFriendlyMessage(e))
            }
        }
    }

    fun initiateDeposit(amountInKsh: Double, phoneNumber: String) {
        val amountInCents = (amountInKsh * 100).toLong()
        Log.d(TAG, "initiateDeposit: $amountInKsh KSh to $phoneNumber")
        if (amountInKsh <= 0) {
            _depositState.value = CustDepositState.Error("Please enter an amount greater than 0.")
            return
        }
        if (phoneNumber.length < 10) {
            _depositState.value = CustDepositState.Error("Please enter a valid phone number.")
            return
        }

        viewModelScope.launch {
            _depositState.value = CustDepositState.Processing
            try {
                val result = initiateStkPushUseCase(
                    consumerKey = com.example.wellme.BuildConfig.MPESA_CONSUMER_KEY,
                    consumerSecret = com.example.wellme.BuildConfig.MPESA_CONSUMER_SECRET,
                    businessShortCode = "174379",
                    passkey = com.example.wellme.BuildConfig.MPESA_PASSKEY,
                    amount = amountInKsh.toInt().toString(),
                    phoneNumber = phoneNumber,
                    callbackUrl = com.example.wellme.BuildConfig.MPESA_CALLBACK_URL,
                    accountReference = "WellMe Donation",
                    transactionDesc = "Community Merchant Donation"
                )

                result.onSuccess { response ->
                    Log.d(TAG, "STK Push request successful. CheckoutRequestId: ${response.checkoutRequestId}")
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

                    _depositState.value = CustDepositState.Success(
                        "STK Push prompt sent to $phoneNumber. Please enter your M-Pesa PIN on your phone to complete your donation."
                    )
                    refreshWallet()
                }.onFailure {
                    Log.e(TAG, "Failed to initiate M-Pesa STK Push", it)
                    _depositState.value = CustDepositState.Error(ErrorMapper.getUserFriendlyMessage(it))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Unexpected error in initiateDeposit", e)
                _depositState.value = CustDepositState.Error(ErrorMapper.getUserFriendlyMessage(e))
            }
        }
    }

    fun donateToMerchant(merchantName: String, tillNumber: String, amountInKsh: Double, phoneNumber: String) {
        initiateDeposit(amountInKsh, phoneNumber)
    }

    fun clearDepositState() {
        _depositState.value = CustDepositState.Idle
    }

    fun resetScanner() {
        Log.d(TAG, "resetScanner")
        _sheetState.value = SheetState.Idle
    }

    fun refreshWallet() {
        Log.d(TAG, "manual refreshWallet for studentId: $studentId")
        _refreshTrigger.value += 1
    }

    fun setObserveRealtime(observe: Boolean) {
        Log.d(TAG, "setObserveRealtime: $observe")
        _observeRealtime.value = observe
    }
}
