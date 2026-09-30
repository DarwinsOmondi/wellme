package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.local.MerchantDao
import com.example.wellme.data.local.MerchantEntity
import com.example.wellme.data.local.TransactionDao
import com.example.wellme.data.local.TransactionEntity
import com.example.wellme.data.remote.model.MerchantDto
import com.example.wellme.data.remote.model.TransactionDto
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.repository.MerchantRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantRepositoryImpl @Inject constructor(
    private val merchantDao: MerchantDao,
    private val transactionDao: TransactionDao,
    private val postgrest: Postgrest
) : MerchantRepository {

    private val TAG = "MerchantRepositoryImpl"

    override fun getMerchant(merchantId: String, observeRealtime: Boolean): Flow<MerchantProfile?> {
        Log.d(TAG, "getMerchant flow requested for: $merchantId (Realtime: $observeRealtime)")
        return merchantDao.getMerchantFlow(merchantId)
            .onStart { 
                Log.d(TAG, "Refreshing merchant: $merchantId")
                refreshMerchant(merchantId) 
            }
            .map { 
                Log.d(TAG, "Merchant data emitted for $merchantId: ${it?.businessName}")
                it?.toDomain() 
            }
    }

    private suspend fun refreshMerchant(merchantId: String) {
        Log.d(TAG, "Refreshing merchant from remote: $merchantId")
        try {
            val dto = postgrest["merchants"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("merchant_id", merchantId)
                    }
                }
                .decodeSingleOrNull<MerchantDto>()
            
            Log.d(TAG, "Remote merchant fetch result for $merchantId: ${dto?.businessName ?: "Not found"}")
            
            dto?.let {
                merchantDao.insertMerchant(MerchantEntity.fromDomain(it.toDomain()))
                Log.d(TAG, "Local merchant data updated for $merchantId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing merchant $merchantId", e)
        }
    }

    override fun getAllMerchants(): Flow<List<MerchantProfile>> {
        Log.d(TAG, "getAllMerchants flow requested")
        return merchantDao.getAllMerchantsFlow().map { list ->
            Log.d(TAG, "All merchants update emitted: ${list.size} items")
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertMerchant(merchant: MerchantProfile) {
        Log.d(TAG, "Inserting/Upserting merchant: ${merchant.merchantId}")
        try {
            postgrest["merchants"].upsert(MerchantDto.fromDomain(merchant))
            Log.d(TAG, "Remote merchant upsert successful")
            merchantDao.insertMerchant(MerchantEntity.fromDomain(merchant))
            Log.d(TAG, "Local merchant insert successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting merchant", e)
        }
    }

    override suspend fun updateMerchantPool(merchantId: String, raisedInCents: Long) {
        Log.d(TAG, "Updating merchant pool for $merchantId to $raisedInCents")
        merchantDao.updateMerchantPool(merchantId, raisedInCents)
    }

    override fun getMerchantTransactions(merchantId: String): Flow<List<Transaction>> {
        Log.d(TAG, "getMerchantTransactions flow requested for: $merchantId")
        return transactionDao.getMerchantTransactionsFlow(merchantId)
            .onStart { 
                Log.d(TAG, "Refreshing transactions for merchant: $merchantId")
                refreshTransactions(merchantId) 
            }
            .map { list ->
                Log.d(TAG, "Merchant transactions update emitted: ${list.size} items")
                list.map { it.toDomain() }
            }
    }

    private suspend fun refreshTransactions(merchantId: String) {
        Log.d(TAG, "Refreshing transactions from remote for merchant: $merchantId")
        try {
            val dtos = postgrest["transactions"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("merchant_id", merchantId)
                    }
                }
                .decodeList<TransactionDto>()
            
            Log.d(TAG, "Remote transactions fetch result for $merchantId: ${dtos.size} items")
            
            dtos.forEach { dto ->
                transactionDao.insertTransaction(TransactionEntity.fromDomain(dto.toDomain()))
            }
            Log.d(TAG, "Local transactions updated for merchant: $merchantId")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing transactions for merchant $merchantId", e)
        }
    }
}
