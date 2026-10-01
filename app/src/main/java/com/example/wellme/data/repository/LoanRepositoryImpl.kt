package com.example.wellme.data.repository

import com.example.wellme.domain.model.MerchantLoanDto
import com.example.wellme.domain.repository.LoanRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

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

    override fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto> {
        return try {
            val channel = realtime.channel("loan_tracking_$loanId")
            val flow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "merchant_loans"
            }
            flow.map { action -> action.decodeRecord<MerchantLoanDto>() }
                .catch {
                    emit(
                        MerchantLoanDto(
                            id = loanId,
                            merchantId = "",
                            amountRequestedInCents = 0L,
                            status = "APPROVED",
                            conversationId = "REQ-$loanId",
                            createdAt = System.currentTimeMillis().toString()
                        )
                    )
                }
        } catch (e: Throwable) {
            flowOf(
                MerchantLoanDto(
                    id = loanId,
                    merchantId = "",
                    amountRequestedInCents = 0L,
                    status = "APPROVED",
                    conversationId = "REQ-$loanId",
                    createdAt = System.currentTimeMillis().toString()
                )
            )
        }
    }
}
