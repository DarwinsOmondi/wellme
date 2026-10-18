package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.domain.model.B2cInitiateResult
import com.example.wellme.domain.model.MerchantLoanDto
import com.example.wellme.domain.repository.LoanRepository
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import javax.inject.Inject

private fun JsonObject.stringValue(key: String): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

private fun JsonObject.booleanValue(key: String): Boolean? =
    (this[key] as? JsonPrimitive)?.booleanOrNull

open class LoanRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val functions: Functions,
) : LoanRepository {

    private val tag = "LoanRepositoryImpl"
    private val json = Json { ignoreUnknownKeys = true }

    private fun parseJsonObject(body: String): JsonObject? =
        runCatching { json.decodeFromString<JsonObject>(body) }.getOrNull()

    private fun serverMessage(body: String, fallback: String): String {
        val parsed = parseJsonObject(body)
        return parsed?.stringValue("message")
            ?: parsed?.stringValue("error")
            ?: fallback
    }

    protected open suspend fun invokeLoanRequestFunction(
        jsonObject: JsonObject,
    ): Pair<Int, String> {
        val response = functions.invoke("request-till-b2c-loan", jsonObject)
        val body = runCatching { response.bodyAsText() }.getOrDefault("")
        return response.status.value to body
    }

    override suspend fun requestLoan(
        amountInCents: Long,
        discountPercentage: Double,
    ): Result<String> {
        if (amountInCents <= 0) {
            return Result.failure(IllegalArgumentException("Loan amount must be greater than zero."))
        }
        if (!discountPercentage.isFinite() || discountPercentage < 0.0) {
            return Result.failure(IllegalArgumentException("Invalid discount percentage."))
        }

        return try {
            val requestBody = buildJsonObject {
                put("amount_requested_in_cents", amountInCents)
                put("discount_percentage", discountPercentage)
                put("till_number", "174379")
            }

            val (status, body) = invokeLoanRequestFunction(requestBody)
            Log.d(tag, "request-till-b2c-loan returned HTTP $status")

            when {
                status == 401 ->
                    Result.failure(Exception("Your session expired. Please sign in again."))

                status == 403 ->
                    Result.failure(Exception("You don't have permission to request this loan."))

                status == 409 ->
                    Result.failure(Exception("A loan request is already pending or was already submitted."))

                status == 400 || status == 422 ->
                    Result.failure(
                        Exception(serverMessage(body, "The loan request failed validation."))
                    )

                status in 500..599 ->
                    Result.failure(
                        Exception(
                            "The server could not confirm the request. Refresh the loan status before retrying."
                        )
                    )

                status in 200..299 -> {
                    val response = parseJsonObject(body)
                    val disbursedLoanId =
                        response?.stringValue("disbursed_loan_id")
                            ?: response?.stringValue("loan_id")
                            ?: response?.stringValue("id")

                    val success = response?.booleanValue("success")
                        ?: !disbursedLoanId.isNullOrBlank()

                    if (success && !disbursedLoanId.isNullOrBlank()) {
                        Result.success(disbursedLoanId)
                    } else {
                        Result.failure(
                            Exception(
                                response?.stringValue("message")
                                    ?: "The server did not return a disbursed loan ID."
                            )
                        )
                    }
                }

                else ->
                    Result.failure(Exception("Loan request failed with HTTP $status."))
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e(tag, "request-till-b2c-loan call failed", e)
            Result.failure(e)
        }
    }

    protected open suspend fun invokeB2cFunction(
        jsonObject: JsonObject,
    ): Pair<Int, String> {
        val response = functions.invoke("merchant-loan-b2c-initiate", jsonObject)
        val body = runCatching { response.bodyAsText() }.getOrDefault("")
        return response.status.value to body
    }

    override suspend fun initiateLoanB2c(
        disbursedLoanId: String,
    ): Result<B2cInitiateResult> {
        if (disbursedLoanId.isBlank()) {
            return Result.failure(IllegalArgumentException("Missing disbursed loan ID."))
        }

        return try {
            val requestBody = buildJsonObject {
                put("disbursed_loan_id", disbursedLoanId)
            }

            val (status, body) = invokeB2cFunction(requestBody)
            Log.d(tag, "merchant-loan-b2c-initiate returned HTTP $status")

            when {
                status == 401 ->
                    Result.failure(Exception("Your session expired. Please sign in again."))

                status == 403 ->
                    Result.failure(Exception("You don't have permission to initiate this payout."))

                status == 409 ->
                    Result.failure(
                        Exception("This disbursement is not pending or has already been submitted.")
                    )

                status in 500..599 ->
                    Result.failure(
                        Exception(
                            "The payout result is uncertain. Refresh the loan status before retrying."
                        )
                    )

                status == 202 -> {
                    val parsed = runCatching {
                        json.decodeFromString<B2cInitiateResult>(body)
                    }.getOrNull()

                    Result.success(
                        (parsed ?: B2cInitiateResult(
                            accepted = true,
                            message = null,
                        )).copy(
                            accepted = true,
                            message = parsed?.message
                                ?: "Payout accepted; status is being reconciled. Do not submit it again yet.",
                        )
                    )
                }

                status in 200..299 -> {
                    val parsed = runCatching {
                        json.decodeFromString<B2cInitiateResult>(body)
                    }.getOrNull()

                    if (parsed == null) {
                        Result.failure(Exception("The payout response could not be read."))
                    } else if (!parsed.accepted) {
                        Result.failure(
                            Exception(parsed.message ?: "The payout request was not accepted.")
                        )
                    } else if (parsed.conversationId.isNullOrBlank()) {
                        Result.success(
                            parsed.copy(
                                message = parsed.message
                                    ?: "Payout accepted; conversation ID is being reconciled. Do not retry yet.",
                            )
                        )
                    } else {
                        Result.success(parsed)
                    }
                }

                else ->
                    Result.failure(Exception("Payout initiation failed with HTTP $status."))
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e(tag, "merchant-loan-b2c-initiate call failed", e)
            Result.failure(e)
        }
    }

    override suspend fun getPendingLoans(): Result<List<MerchantLoanDto>> {
        return try {
            val loans = postgrest
                .from("disbursed_merchant_loans")
                .select {
                    filter {
                        eq("status", "PENDING")
                    }
                }
                .decodeList<MerchantLoanDto>()

            Result.success(loans)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch pending loans", e)
            Result.failure(e)
        }
    }

    override suspend fun getDisbursedLoans(
        merchantId: String,
    ): Result<List<MerchantLoanDto>> {
        return try {
            val loans = postgrest
                .from("disbursed_merchant_loans")
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                    }
                }
                .decodeList<MerchantLoanDto>()

            Result.success(loans)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch disbursed loans", e)
            Result.failure(e)
        }
    }

    override suspend fun markLoanAsDisbursed(merchantId: String): Result<Unit> =
        Result.failure(Exception("Loan status updates are restricted to backend handlers."))

    override fun observeLoanLifecycle(loanId: String): Flow<MerchantLoanDto> =
        channelFlow {
            require(loanId.isNotBlank()) { "Loan ID is required." }

            val channel = realtime.channel("loan_tracking_$loanId")
            val updates = channel.postgresChangeFlow<PostgresAction.Update>(
                schema = "public",
            ) {
                table = "disbursed_merchant_loans"
            }

            val collector = launch {
                updates.collect { action ->
                    val dto = action.decodeRecord<MerchantLoanDto>()
                    if (dto.id == loanId) {
                        send(dto)
                    }
                }
            }

            try {
                realtime.connect()
                channel.subscribe()
                collector.join()
            } finally {
                collector.cancelAndJoin()
                realtime.removeChannel(channel)
            }
        }
}
