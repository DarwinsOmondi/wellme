package com.example.wellme.data.repository

import android.util.Log
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

    private val TAG = "LoanRepositoryImpl"

    override suspend fun requestLoan(merchantId: String, amountInCents: Long, discountPercentage: Double): Result<String> {
        return try {
            val response = postgrest.rpc(
                function = "request_till_b2c_loan",
                parameters = buildJsonObject {
                    put("p_merchant_id", merchantId)
                    put("p_business_name", "Vendor $merchantId")
                    put("p_amount_requested_in_cents", amountInCents)
                    put("p_discount_percentage", discountPercentage * 100)
                    put("p_till_number", "174379")
                }
            )
            val resultData = response.data
            Log.d(TAG, "request_till_b2c_loan response: $resultData")
            Result.success(resultData)
        } catch (e: Exception) {
            Log.e(TAG, "request_till_b2c_loan failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingLoans(): Result<List<MerchantLoanDto>> {
        return try {
            val loans = postgrest.from("disbursed_merchant_loans")
                .select()
                .decodeList<MerchantLoanDto>()
            Result.success(loans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDisbursedLoans(merchantId: String): Result<List<MerchantLoanDto>> {
        return try {
            val loans = postgrest.from("disbursed_merchant_loans")
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                        eq("status", "DISBURSED")
                    }
                }
                .decodeList<MerchantLoanDto>()
            Result.success(loans)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch disbursed loans for merchant $merchantId", e)
            Result.failure(e)
        }
    }

    override suspend fun markLoanAsDisbursed(merchantId: String): Result<Unit> {
        return try {
            postgrest.from("disbursed_merchant_loans")
                .update(mapOf("status" to "DISBURSED")) {
                    filter {
                        eq("merchant_id", merchantId)
                        eq("status", "PENDING")
                    }
                }
            Log.d(TAG, "Marked merchant $merchantId loans as DISBURSED")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark loan as disbursed", e)
            Result.failure(e)
        }
    }

    override fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto> {
        return try {
            val channel = realtime.channel("loan_tracking_$loanId")
            val flow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "disbursed_merchant_loans"
            }
            flow.map { action -> 
                action.decodeRecord<MerchantLoanDto>()
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
        } catch (_: Throwable) {
            flowOf(
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
