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
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.seconds

sealed interface SheetState {
    object Idle : SheetState
    data class Scanned(val merchant: MerchantProfile, val originalAmountInCents: Long, val discountAppliedInCents: Long, val netAmountInCents: Long) : SheetState
    object Processing : SheetState
    data class Success(val transaction: Transaction) : SheetState
    data class Error(val message: String) : SheetState
}

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
        viewModelScope.launch {
            _sheetState.value = SheetState.Processing
            val result = initiateStkPushUseCase(
                consumerKey = com.example.wellme.BuildConfig.MPESA_CONSUMER_KEY,
                consumerSecret = com.example.wellme.BuildConfig.MPESA_CONSUMER_SECRET,
                businessShortCode = "174379",
                passkey = com.example.wellme.BuildConfig.MPESA_PASSKEY,
                amount = amountInKsh.toInt().toString(),
                phoneNumber = phoneNumber,
                callbackUrl = com.example.wellme.BuildConfig.MPESA_CALLBACK_URL,
                accountReference = "WellMe Deposit",
                transactionDesc = "Student Wallet Deposit"
            )

            result.onSuccess { response ->
                Log.d(TAG, "STK Push request successful. CheckoutRequestId: ${response.checkoutRequestId}")
                // Step B: Log to Supabase pending_payments
                val pendingPayment = PendingPayment(
                    checkoutRequestId = response.checkoutRequestId,
                    studentId = studentId,
                    amountInCents = amountInCents,
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
                walletRepository.logPendingPayment(pendingPayment)

                // Step C: Observe real-time update with a timeout
                Log.d(TAG, "Observing real-time updates for checkoutRequestId: ${response.checkoutRequestId}")
                setObserveRealtime(true)
                withTimeoutOrNull(60.seconds) {
                    walletRepository.observePendingPayment(response.checkoutRequestId)
                        .first { it.checkoutRequestId == response.checkoutRequestId && it.status != "PENDING" }
                        .let { updatedPayment ->
                            Log.d(TAG, "Real-time update received. Status: ${updatedPayment.status}")
                            setObserveRealtime(false)
                            if (updatedPayment.status == "COMPLETED") {
                                Log.d(TAG, "Deposit completed. Syncing wallet...")
                                refreshWallet() // Trigger the Supabase -> Room -> UI flow

                                _sheetState.value = SheetState.Success(
                                    Transaction(
                                        transactionId = updatedPayment.checkoutRequestId,
                                        amountInCents = updatedPayment.amountInCents,
                                        originalAmountInCents = updatedPayment.amountInCents,
                                        discountAppliedInCents = 0L,
                                        timestamp = updatedPayment.timestamp,
                                        type = TransactionType.STIPEND,
                                        merchantId = "mpesa",
                                        studentId = studentId
                                    )
                                )
                            } else {
                                _sheetState.value = SheetState.Error("Payment failed: ${updatedPayment.status}")
                            }
                        }
                } ?: run {
                    Log.e(TAG, "Payment timed out for checkoutRequestId: ${response.checkoutRequestId}")
                    _sheetState.value = SheetState.Error("Payment timed out. Please check your M-Pesa.")
                }
            }.onFailure {
                Log.e(TAG, "Failed to initiate M-Pesa STK Push", it)
                _sheetState.value = SheetState.Error(it.message ?: "Failed to initiate M-Pesa deposit")
            }
        }
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
