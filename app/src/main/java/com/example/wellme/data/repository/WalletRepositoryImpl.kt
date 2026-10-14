package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.local.StudentWalletEntity
import com.example.wellme.data.local.TransactionEntity
import com.example.wellme.data.local.WalletDao
import com.example.wellme.data.local.TransactionDao
import com.example.wellme.data.remote.model.StudentWalletDto
import com.example.wellme.domain.model.CheckoutItem
import com.example.wellme.domain.model.CheckoutResult
import com.example.wellme.domain.model.PendingPayment
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.repository.WalletRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withTimeout
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class PaymentAttemptStatusDto(
    @SerialName("id") val id: String,
    @SerialName("status") val status: String
)

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao,
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val auth: Auth,
    private val functions: Functions
) : WalletRepository {

    private val tag = "WalletRepositoryImpl"

    @OptIn(DelicateCoroutinesApi::class)
    override fun getWallet(studentId: String, observeRealtime: Boolean): Flow<StudentWallet?> =
        flow {
            Log.i(
                tag,
                "[Wallet] Starting flow for student: $studentId (Realtime: $observeRealtime)"
            )

            syncWallet(studentId)

            if (observeRealtime) {
                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    setupRealtimeWallet(studentId)
                }
            }

            emitAll(walletDao.getWalletFlow(studentId).map { it?.toDomain() })
        }.flowOn(Dispatchers.IO)

    private suspend fun syncWallet(studentId: String) {
        try {
            val list = withTimeout(10000L.milliseconds) {
                postgrest["student_wallets"]
                    .select { filter { eq("student_id", studentId) } }
                    .decodeList<StudentWalletDto>()
            }
            if (list.isNotEmpty()) {
                val domain = list.first().toDomain()
                walletDao.insertWallet(StudentWalletEntity.fromDomain(domain))
            }
        } catch (e: Exception) {
            Log.e(tag, "[Sync] Error during sync", e)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun setupRealtimeWallet(studentId: String) {
        val channelName = "wallet_$studentId"
        val channel = realtime.channel(channelName)

        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "student_wallets"
        }.onEach { action ->
            val record = action.decodeRecord<StudentWalletDto>()
            walletDao.insertWallet(StudentWalletEntity.fromDomain(record.toDomain()))
        }.launchIn(kotlinx.coroutines.GlobalScope)

        try {
            realtime.connect()
            channel.subscribe()
        } catch (e: Exception) {
            Log.e(tag, "[Realtime] Subscription failed", e)
        }
    }

    override suspend fun refreshWallet(studentId: String) {
        try {
            val list = postgrest["student_wallets"]
                .select { filter { eq("student_id", studentId) } }
                .decodeList<StudentWalletDto>()
            if (list.isNotEmpty()) {
                val domain = list.first().toDomain()
                walletDao.insertWallet(StudentWalletEntity.fromDomain(domain))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing wallet for $studentId", e)
        }
    }

    override suspend fun insertWallet(wallet: StudentWallet) {
        try {
            walletDao.insertWallet(StudentWalletEntity.fromDomain(wallet))
        } catch (e: Exception) {
            Log.e(tag, "Error inserting wallet", e)
        }
    }

    override suspend fun updateWalletBalance(studentId: String, newBalanceInCents: Long) {
        try {
            walletDao.updateWalletBalance(studentId, newBalanceInCents)
        } catch (e: Exception) {
            Log.e(tag, "Error updating local balance", e)
        }
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionDao.getTransactionsFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        try {
            transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
        } catch (e: Exception) {
            Log.e(tag, "Error inserting transaction", e)
        }
    }

    override suspend fun logPendingPayment(payment: PendingPayment) {
        Log.d(tag, "logPendingPayment stubbed")
    }

    override fun observePendingPayment(checkoutRequestId: String): Flow<PendingPayment> =
        flow<PendingPayment> {
            Log.w(
                tag,
                "Client-side observation of pending payments is disabled pending backend read contract."
            )
        }.flowOn(Dispatchers.IO)

    override suspend fun initiateWalletTopUp(
        amountInCents: Long,
        phoneNumber: String,
        idempotencyKey: String
    ): Result<String> {
        return try {
            val jsonString = buildJsonObject {
                put("amount_in_cents", amountInCents)
                put("phone_number", phoneNumber)
                put("idempotency_key", idempotencyKey)
            }.toString()

            val response = functions.invoke("wallet-topup-initiate", jsonString)
            val responseString = response.bodyAsText()
            val responseBody =
                kotlinx.serialization.json.Json.decodeFromString<JsonObject>(responseString)
            val paymentAttemptId =
                responseBody.get("payment_attempt_id")?.toString()?.replace("\"", "")
                    ?: responseBody.get("id")?.toString()?.replace("\"", "")
                    ?: idempotencyKey
            Result.success(paymentAttemptId)
        } catch (e: Exception) {
            Log.e(tag, "Error calling wallet-topup-initiate Edge Function", e)
            Result.failure(e)
        }
    }

    override suspend fun checkoutWithWallet(
        items: List<CheckoutItem>,
        idempotencyKey: String
    ): Result<CheckoutResult> {
        return try {
            val result = postgrest.rpc(
                function = "checkout_with_wallet",
                parameters = buildJsonObject {
                    put("p_items", kotlinx.serialization.json.Json.encodeToJsonElement(items))
                    put("p_idempotency_key", idempotencyKey)
                }
            ).decodeAs<CheckoutResult>()
            Result.success(result)
        } catch (e: Exception) {
            Log.e(tag, "Error executing checkout_with_wallet RPC", e)
            Result.failure(e)
        }
    }

    override suspend fun getPaymentAttemptStatus(paymentAttemptId: String): Result<String> {
        return try {
            val record = postgrest["payment_attempts"]
                .select { filter { eq("id", paymentAttemptId) } }
                .decodeSingleOrNull<PaymentAttemptStatusDto>()
            val status = record?.status ?: "PENDING"
            Result.success(status)
        } catch (e: Exception) {
            Log.e(tag, "Error reading payment_attempts status (Check RLS permissions)", e)
            Result.failure(e)
        }
    }
}
