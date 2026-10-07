package com.example.wellme.domain.usecase

import com.example.wellme.domain.model.LoanLifecycleState
import com.example.wellme.domain.model.MerchantLoanDto
import com.example.wellme.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class RequestLoanUseCase @Inject constructor(
    private val loanRepository: LoanRepository
) {
    suspend fun execute(merchantId: String, amountInKsh: Double, yieldPercentage: Double): Flow<LoanLifecycleState> {
        val amountInCents = (amountInKsh * 100).toLong()
        
        return loanRepository.requestLoan(merchantId, amountInCents, yieldPercentage)
            .fold(
                onSuccess = { resultData ->
                    loanRepository.observeLoanLifecycle(resultData)
                        .map { dto ->
                            mapDtoToState(dto)
                        }
                        .onStart { 
                            emit(LoanLifecycleState.RequestAccepted(resultData, null)) 
                        }
                },
                onFailure = { error ->
                    kotlinx.coroutines.flow.flow {
                        emit(LoanLifecycleState.OperationalError(error.message ?: "Unknown error"))
                    }
                }
            )
            .catch { e ->
                emit(LoanLifecycleState.OperationalError(e.message ?: "Stream error"))
            }
    }

    private fun mapDtoToState(dto: MerchantLoanDto): LoanLifecycleState {
        return when (dto.status) {
            "PENDING" -> LoanLifecycleState.RequestAccepted(dto.id, dto.conversationId)
            "APPROVED" -> LoanLifecycleState.RequestAccepted(dto.id, dto.conversationId)
            "DISBURSED" -> LoanLifecycleState.DisbursedSuccess(dto.amountRequestedInCents)
            "FAILED" -> LoanLifecycleState.OperationalError("Loan disbursement failed")
            else -> LoanLifecycleState.OperationalError("Unknown status: ${dto.status}")
        }
    }
}
