package com.example.wellme.domain.repository

import com.example.wellme.domain.model.B2cInitiateResult
import com.example.wellme.domain.model.MerchantLoanDto
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    suspend fun requestLoan(amountInCents: Long, discountPercentage: Double): Result<String>
    suspend fun initiateLoanB2c(disbursedLoanId: String): Result<B2cInitiateResult>
    suspend fun getPendingLoans(): Result<List<MerchantLoanDto>>
    suspend fun getDisbursedLoans(merchantId: String): Result<List<MerchantLoanDto>>
    suspend fun markLoanAsDisbursed(merchantId: String): Result<Unit>
    fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto>
}
