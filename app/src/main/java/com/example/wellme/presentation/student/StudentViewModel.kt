package com.example.wellme.presentation.student

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.PendingPayment
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.WalletRepository
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
    data class Pending(val checkoutRequestId: String, val message: String) : SheetState
    data class Success(val transaction: Transaction) : SheetState
    data class Error(val message: String) : SheetState
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
    private val processPaymentUseCase: ProcessPaymentUseCase
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
                val checkoutResult = processPaymentUseCase(
                    studentId = studentId,
                    merchantId = currentState.merchant.merchantId,
                    originalAmountInCents = currentState.originalAmountInCents
                )
                Log.d(TAG, "Payment successful. Order ID: ${checkoutResult.orderId}. Syncing wallet...")
                refreshWallet()
                val transaction = Transaction(
                    transactionId = checkoutResult.orderId,
                    amountInCents = checkoutResult.totalInCents,
                    originalAmountInCents = checkoutResult.subtotalInCents,
                    discountAppliedInCents = checkoutResult.discountInCents,
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.PAYMENT,
                    merchantId = currentState.merchant.merchantId,
                    studentId = studentId
                )
                _sheetState.value = SheetState.Success(transaction)
            } catch (e: Exception) {
                Log.e(TAG, "Payment failed", e)
                _sheetState.value = SheetState.Error(ErrorMapper.getUserFriendlyMessage(e))
            }
        }
    }

    private var pendingJob: kotlinx.coroutines.Job? = null

    fun observePendingCheckout(checkoutRequestId: String) {
        pendingJob?.cancel()
        pendingJob = viewModelScope.launch {
            walletRepository.observePendingPayment(checkoutRequestId)
                .collect { pending ->
                    Log.d(TAG, "Observed pending payment status: ${pending.status}")
                    if (pending.status.equals("SUCCESS", ignoreCase = true)) {
                        _sheetState.value = SheetState.Success(
                            Transaction(
                                transactionId = pending.checkoutRequestId,
                                amountInCents = pending.amountInCents,
                                originalAmountInCents = pending.amountInCents,
                                discountAppliedInCents = 0L,
                                timestamp = System.currentTimeMillis(),
                                type = TransactionType.STIPEND,
                                merchantId = "mpesa",
                                studentId = studentId
                            )
                        )
                        refreshWallet()
                    } else if (pending.status.equals("FAILED", ignoreCase = true)) {
                        _sheetState.value = SheetState.Error("M-Pesa payment failed or was cancelled.")
                    }
                }
        }
    }

    private var currentTopUpIdempotencyKey: String? = null

    fun initiateDeposit(amountInKsh: Double, phoneNumber: String) {
        val amountInCents = (amountInKsh * 100).roundToLong()
        if (amountInCents < 100 || amountInCents % 100 != 0L) {
            _sheetState.value = SheetState.Error("amount_in_cents must be at least 100 and a whole KES amount in cents")
            return
        }
        if (phoneNumber.isBlank() || phoneNumber.length < 10) {
            _sheetState.value = SheetState.Error("Please enter a valid phone number.")
            return
        }

        val idempotencyKey = currentTopUpIdempotencyKey ?: java.util.UUID.randomUUID().toString().also { currentTopUpIdempotencyKey = it }

        viewModelScope.launch {
            _sheetState.value = SheetState.Processing
            try {
                val result = walletRepository.initiateWalletTopUp(amountInCents, phoneNumber, idempotencyKey)
                if (result.isSuccess) {
                    val paymentAttemptId = result.getOrNull() ?: idempotencyKey
                    Log.d(TAG, "Wallet top-up initiated successfully. Payment attempt ID: $paymentAttemptId")
                    _sheetState.value = SheetState.Pending(
                        paymentAttemptId,
                        "STK Push prompt sent to $phoneNumber. Please approve on your phone."
                    )
                    observePendingCheckout(paymentAttemptId)
                } else {
                    val exception = result.exceptionOrNull() ?: Exception("Unknown error initiating top-up")
                    Log.e(TAG, "Wallet top-up initiation failed", exception)
                    _sheetState.value = SheetState.Error(ErrorMapper.getUserFriendlyMessage(exception))
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Unexpected error in initiateDeposit", e)
                _sheetState.value = SheetState.Error(ErrorMapper.getUserFriendlyMessage(e))
            }
        }
    }

    fun donateToMerchant(merchantName: String, tillNumber: String, amountInKsh: Double, phoneNumber: String) {
        initiateDeposit(amountInKsh, phoneNumber)
    }

    fun resetScanner() {
        Log.d(TAG, "resetScanner")
        pendingJob?.cancel()
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
