package com.example.wellme.presentation.merchant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.model.LoanLifecycleState
import com.example.wellme.domain.usecase.GetMerchantPoolProgressUseCase
import com.example.wellme.domain.usecase.ProcessPaymentUseCase
import com.example.wellme.domain.usecase.RequestLoanUseCase
import com.example.wellme.util.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MerchantViewModel @Inject constructor(
    private val merchantRepository: MerchantRepository,
    private val authRepository: AuthRepository,
    private val getMerchantPoolProgressUseCase: GetMerchantPoolProgressUseCase,
    private val requestLoanUseCase: RequestLoanUseCase,
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {

    val merchantId: String = authRepository.getCurrentUser()?.id ?: ""

    val merchant: StateFlow<MerchantProfile?> = merchantRepository.getMerchant(merchantId, false)
        .onEach { profile ->
            profile?.let {
                amountInput = (it.poolTargetInCents / 100).toString()
                selectedYield = it.discountTier
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var amountInput by mutableStateOf("")
    var selectedYield by mutableDoubleStateOf(0.15)
    var loanState by mutableStateOf<LoanLifecycleState>(LoanLifecycleState.Idle)
        private set

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed interface UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent
        object Success : UiEvent
    }

    fun onAmountInputChange(value: String) { amountInput = value }
    fun onSelectedYieldChange(value: Double) { selectedYield = value }

    fun submitCapitalRequest() {
        val amount = amountInput.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowSnackbar("Please enter a valid funding amount")) }
            return
        }

        viewModelScope.launch {
            loanState = LoanLifecycleState.SubmittingRpc
            try {
                requestLoanUseCase.execute(merchantId, amount, selectedYield).collect { state ->
                    loanState = state
                    if (state is LoanLifecycleState.OperationalError) {
                        _uiEvent.emit(UiEvent.ShowSnackbar(state.message))
                    } else if (state is LoanLifecycleState.DisbursedSuccess) {
                        _uiEvent.emit(UiEvent.ShowSnackbar("Funding request processed successfully!"))
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("MerchantViewModel", "Failed to submit funding request", e)
                val msg = ErrorMapper.getUserFriendlyMessage(e)
                loanState = LoanLifecycleState.OperationalError(msg)
                _uiEvent.emit(UiEvent.ShowSnackbar(msg))
            }
        }
    }

    val transactions: StateFlow<List<Transaction>> = merchantRepository.getMerchantTransactions(merchantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val poolProgress: StateFlow<Float> = getMerchantPoolProgressUseCase(merchantId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0f)

    fun processCustomerPayment(studentId: String, originalAmountInCents: Long) {
        viewModelScope.launch {
            try {
                processPaymentUseCase(
                    studentId = studentId,
                    merchantId = merchantId,
                    originalAmountInCents = originalAmountInCents
                )
                _uiEvent.emit(UiEvent.ShowSnackbar("Payment processed successfully!"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar(ErrorMapper.getUserFriendlyMessage(e)))
            }
        }
    }
}
