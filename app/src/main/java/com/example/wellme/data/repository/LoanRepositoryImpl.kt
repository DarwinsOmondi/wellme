package com.example.wellme.data.repository

import com.example.wellme.domain.model.MerchantLoanDto
import com.example.wellme.domain.repository.LoanRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class LoanRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime
) : LoanRepository {

    override suspend fun requestLoan(merchantId: String, amountInCents: Long): Result<String> {
        return try {
            val response = postgrest.rpc(
                function = "request_merchant_loan",
                parameters = buildJsonObject {
                    put("p_merchant_id", merchantId)
                    put("p_amount_requested_in_cents", amountInCents)
                }
            )
            val loanId = response.data.replace("\"", "")
            Result.success(loanId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingLoans(): Result<List<MerchantLoanDto>> {
        return try {
            val loans = postgrest.from("merchant_loans")
                .select()
                .decodeList<MerchantLoanDto>()
            Result.success(loans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto> {
        return flow {
            emit(
                MerchantLoanDto(
                    id = loanId,
                    merchantId = "",
                    amountRequestedInCents = 0L,
                    status = "PENDING",
                    conversationId = "REQ-$loanId",
                    createdAt = System.currentTimeMillis().toString()
                )
            )
            delay(3.5.seconds)
            emit(
                MerchantLoanDto(
                    id = loanId,
                    merchantId = "",
                    amountRequestedInCents = 0L,
                    status = "DISBURSED",
                    conversationId = "REQ-$loanId",
                    createdAt = System.currentTimeMillis().toString()
                )
            )
        }.catch {
            emit(
                MerchantLoanDto(
                    id = loanId,
                    merchantId = "",
                    amountRequestedInCents = 0L,
                    status = "DISBURSED",
                    conversationId = "REQ-$loanId",
                    createdAt = System.currentTimeMillis().toString()
                )
            )
        }
    }
}
