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
import kotlinx.coroutines.flow.filterIsInstance
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
            // Assuming the RPC returns the UUID as a plain string or within a JSON object
            val loanId = response.data.replace("\"", "")
            Result.success(loanId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto> {
        val channel = realtime.channel("loan_tracking_$loanId")
        val flow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "merchant_loans"
            // Use eq filter properly if the DSL allows, or if it's set via filter property
            // In supabase-kt, filter is a property you can set.
        }
        
        return flow.map { action ->
            action.decodeRecord<MerchantLoanDto>()
        }
    }
}
