package com.example.wellme.presentation.merchant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wellme.domain.model.LoanLifecycleState
import com.example.wellme.domain.usecase.RequestLoanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val requestLoanUseCase: RequestLoanUseCase
) : ViewModel() {

    private val _loanState = MutableStateFlow<LoanLifecycleState>(LoanLifecycleState.Idle)
    val loanState: StateFlow<LoanLifecycleState> = _loanState.asStateFlow()

    var amountInput by mutableStateOf("")

    fun requestLoan(merchantId: String) {
        val amount = amountInput.toDoubleOrNull() ?: return
        
        viewModelScope.launch {
            _loanState.value = LoanLifecycleState.SubmittingRpc
            requestLoanUseCase.execute(merchantId, amount).collect { state ->
                _loanState.value = state
            }
        }
    }
}
