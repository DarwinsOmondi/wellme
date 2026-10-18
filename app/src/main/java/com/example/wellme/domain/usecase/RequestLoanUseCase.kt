package com.example.wellme.domain.usecase

import com.example.wellme.domain.model.LoanLifecycleState
import com.example.wellme.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RequestLoanUseCase @Inject constructor(
    private val loanRepository: LoanRepository,
) {
    suspend fun execute(merchantId: String, amountInKsh: Double, yieldPercentage: Double): Flow<LoanLifecycleState> {
        val amountInCents = (amountInKsh * 100).toLong()
        
        return loanRepository.requestLoan(amountInCents, yieldPercentage)
            .fold(
                onSuccess = { loanId ->
                    flow {
                        emit(LoanLifecycleState.RequestAccepted(loanId, null))
                    }
                },
                onFailure = { error ->
                    flow {
                        emit(LoanLifecycleState.OperationalError(error.message ?: "Unknown error"))
                    }
                },
            )
            .catch { e ->
                emit(LoanLifecycleState.OperationalError(e.message ?: "Stream error"))
            }
    }
}
