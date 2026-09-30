package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.local.StudentWalletEntity
import com.example.wellme.data.local.TransactionEntity
import com.example.wellme.data.local.WalletDao
import com.example.wellme.data.local.TransactionDao
import com.example.wellme.data.remote.model.PendingPaymentDto
import com.example.wellme.data.remote.model.StudentWalletDto
import com.example.wellme.data.remote.model.TransactionDto
import com.example.wellme.domain.model.PendingPayment
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.repository.WalletRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val transactionDao: TransactionDao,
    private val postgrest: Postgrest,
    private val realtime: Realtime,
    private val auth: Auth
) : WalletRepository {

    private val tag = "WalletRepositoryImpl"

    override fun getWallet(studentId: String, observeRealtime: Boolean): Flow<StudentWallet?> = flow {
        Log.i(tag, "[Wallet] Starting flow for student: $studentId (Realtime: $observeRealtime)")
        
        // 1. Initial Sync (Remote -> Local)
        syncWallet(studentId)

        // 2. Start Realtime if requested (it will write to Room)
        if (observeRealtime) {
            // We use a separate scope for realtime to avoid blocking the emission
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                setupRealtimeWallet(studentId)
            }
        }

        // 3. Emit live from Local DB (The single source of truth)
        Log.d(tag, "[Wallet] Emitting live flow from Room DB")
        emitAll(walletDao.getWalletFlow(studentId).map { it?.toDomain() })
    }.flowOn(Dispatchers.IO)

    private suspend fun syncWallet(studentId: String) {
        try {
            val user = auth.currentUserOrNull()
            Log.d(tag, "[Sync] Current User: ${user?.email ?: "NOT LOGGED IN"}")
            Log.d(tag, "[Sync] Fetching remote wallet for student: $studentId")
            
            val list = withTimeout(10000L) {
                postgrest["student_wallets"]
                    .select { filter { eq("student_id", studentId) } }
                    .decodeList<StudentWalletDto>()
            }
            
            Log.i(tag, "[Sync] Remote fetch result: ${list.size} rows found for $studentId")
            
            if (list.isNotEmpty()) {
                val domain = list.first().toDomain()
                Log.i(tag, "[Sync] Data: Balance=${domain.balanceInCents}")
                walletDao.insertWallet(StudentWalletEntity.fromDomain(domain))
                Log.d(tag, "[Sync] Local Room DB updated")
            } else {
                Log.w(tag, "[Sync] No matching record found in Supabase student_wallets table for ID $studentId. Check RLS or if the row exists.")
            }
        } catch (e: Exception) {
            Log.e(tag, "[Sync] Error during sync", e)
        }
    }

    private suspend fun setupRealtimeWallet(studentId: String) {
        val channelName = "wallet_$studentId"
        val channel = realtime.channel(channelName)
        
        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "student_wallets"
        }.onEach { action ->
            val record = action.decodeRecord<StudentWalletDto>()
            Log.i(tag, "[Realtime] Wallet update detected. Syncing to Room.")
            walletDao.insertWallet(StudentWalletEntity.fromDomain(record.toDomain()))
        }.launchIn(kotlinx.coroutines.GlobalScope)

        try {
            realtime.connect()
            channel.subscribe()
            Log.d(tag, "[Realtime] Subscribed to $channelName")
        } catch (e: Exception) {
            Log.e(tag, "[Realtime] Subscription failed", e)
        }
    }

    override suspend fun refreshWallet(studentId: String) {
        Log.d(tag, "Refreshing wallet from remote for: $studentId")
        try {
            val dto = postgrest["student_wallets"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("student_id", studentId)
                    }
                }
                .decodeSingleOrNull<StudentWalletDto>()
            
            Log.d(tag, "Remote wallet fetch result for $studentId: ${dto?.balanceInCents ?: "Not found"}")
            
            dto?.let {
                walletDao.insertWallet(StudentWalletEntity.fromDomain(it.toDomain()))
                Log.d(tag, "Local wallet updated for $studentId")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error refreshing wallet for $studentId", e)
        }
    }

    override suspend fun insertWallet(wallet: StudentWallet) {
        Log.d(tag, "Inserting/Upserting wallet: ${wallet.studentId}")
        try {
            postgrest["student_wallets"].upsert(StudentWalletDto.fromDomain(wallet))
            Log.d(tag, "Remote wallet upsert successful")
            walletDao.insertWallet(StudentWalletEntity.fromDomain(wallet))
            Log.d(tag, "Local wallet insert successful")
        } catch (e: Exception) {
            Log.e(tag, "Error inserting wallet", e)
        }
    }

    override suspend fun updateWalletBalance(studentId: String, newBalanceInCents: Long) {
        Log.i(tag, "[Transaction] Updating wallet balance for $studentId to $newBalanceInCents")
        // 1. Update Remote (Supabase)
        try {
            Log.d(tag, "[Transaction] Sending update to Supabase...")
            postgrest["student_wallets"].update(
                mapOf("balance_in_cents" to newBalanceInCents)
            ) {
                filter { eq("student_id", studentId) }
            }
            Log.i(tag, "[Transaction] Remote balance update successful")
        } catch (e: Exception) {
            Log.e(tag, "[Transaction] Error updating remote balance", e)
        }

        // 2. Update Local (Room) - Room remains source of truth for UI
        try {
            Log.d(tag, "[Transaction] Updating local Room DB balance...")
            walletDao.updateWalletBalance(studentId, newBalanceInCents)
            Log.i(tag, "[Transaction] Local balance update successful")
        } catch (e: Exception) {
            Log.e(tag, "[Transaction] Error updating local balance", e)
        }
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        Log.d(tag, "getTransactions flow requested")
        return transactionDao.getTransactionsFlow().map { list ->
            Log.d(tag, "Transactions update emitted: ${list.size} items")
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        Log.d(tag, "Inserting transaction: ${transaction.transactionId}")
        try {
            postgrest["transactions"].insert(TransactionDto.fromDomain(transaction))
            Log.d(tag, "Remote transaction insert successful")
            transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))
            Log.d(tag, "Local transaction insert successful")
        } catch (e: Exception) {
            Log.e(tag, "Error inserting transaction", e)
        }
    }

    override suspend fun logPendingPayment(payment: PendingPayment) {
        Log.d(tag, "Logging pending payment: ${payment.checkoutRequestId}")
        try {
            postgrest["pending_payments"].insert(PendingPaymentDto.fromDomain(payment))
            Log.d(tag, "Pending payment logged successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error logging pending payment", e)
        }
    }

    override fun observePendingPayment(checkoutRequestId: String): Flow<PendingPayment> = flow {
        Log.i(tag, "[Observe] Starting robust observation for ID: $checkoutRequestId")
        
        // 1. Setup real-time updates channel
        val channelName = "payment_$checkoutRequestId"
        Log.d(tag, "[Realtime] Creating channel: $channelName")
        val channel = realtime.channel(channelName)
        
        val updateFlow = channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "pending_payments"
        }.map { action ->
            val record = action.decodeRecord<PendingPaymentDto>()
            Log.i(tag, "[Realtime] Change detected for $checkoutRequestId. New Status: ${record.status}")
            record.toDomain()
        }

        try {
            Log.d(tag, "[Realtime] Subscribing to channel: $channelName")
            realtime.connect()
            withTimeout(5000L) { // Reduced timeout
                channel.subscribe()
            }
            Log.d(tag, "[Realtime] Subscribed successfully to $channelName")
        } catch (e: Exception) {
            Log.e(tag, "[Realtime] Subscription to $channelName timed out or failed (timeout or connection issue). Polling will continue.", e)
        }

        // 2. Initial Polling & Fallback Loop
        Log.d(tag, "[Polling] Starting fallback loop for $checkoutRequestId")
        var attempt = 0
        while (true) {
            attempt++
            try {
                Log.d(tag, "[Polling] Attempt $attempt for $checkoutRequestId...")
                val current = postgrest["pending_payments"]
                    .select { filter { eq("checkout_request_id", checkoutRequestId) } }
                    .decodeSingleOrNull<PendingPaymentDto>()
                
                if (current != null) {
                    Log.i(tag, "[Polling] Found record for $checkoutRequestId. Status: ${current.status}")
                    emit(current.toDomain())
                    if (current.status != "PENDING") {
                        Log.i(tag, "[Observe] Final state reached via polling: ${current.status}. Exiting loop.")
                        break 
                    }
                } else {
                    Log.w(tag, "[Polling] No record found yet for $checkoutRequestId")
                }
            } catch (e: Exception) {
                Log.e(tag, "[Polling] Error during attempt $attempt for $checkoutRequestId", e)
            }
            
            Log.v(tag, "[Polling] Waiting 3s before next check...")
            delay(3000)
        }
        
        Log.d(tag, "[Observe] Handing over to realtime update flow for $checkoutRequestId")
        emitAll(updateFlow)
    }.flowOn(Dispatchers.IO)
}
