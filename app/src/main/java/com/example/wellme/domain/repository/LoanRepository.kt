package com.example.wellme.domain.repository

import com.example.wellme.domain.model.MerchantLoanDto
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    suspend fun requestLoan(merchantId: String, amountInCents: Long): Result<String>
    suspend fun getPendingLoans(): Result<List<MerchantLoanDto>>
    fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto>
}
