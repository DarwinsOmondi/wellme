package com.example.wellme.domain.repository

import com.example.wellme.domain.model.MerchantLoanDto
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    suspend fun requestLoan(merchantId: String, amountInCents: Long, discountPercentage: Double): Result<String>
    suspend fun getPendingLoans(): Result<List<MerchantLoanDto>>
    suspend fun markLoanAsDisbursed(merchantId: String): Result<Unit>
    fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto>
}
