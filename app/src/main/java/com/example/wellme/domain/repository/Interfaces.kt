package com.example.wellme.domain.repository

import com.example.wellme.domain.model.MerchantItem
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.PendingPayment
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getWallet(studentId: String, observeRealtime: Boolean = false): Flow<StudentWallet?>
    suspend fun refreshWallet(studentId: String)
    suspend fun insertWallet(wallet: StudentWallet)
    suspend fun updateWalletBalance(studentId: String, newBalanceInCents: Long)
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)

    suspend fun logPendingPayment(payment: PendingPayment)
    fun observePendingPayment(checkoutRequestId: String): Flow<PendingPayment>
}

interface MerchantRepository {
    fun getMerchant(merchantId: String, observeRealtime: Boolean = false): Flow<MerchantProfile?>
    fun getAllMerchants(): Flow<List<MerchantProfile>>
    suspend fun insertMerchant(merchant: MerchantProfile)
    suspend fun updateMerchantPool(merchantId: String, raisedInCents: Long)
    fun getMerchantTransactions(merchantId: String): Flow<List<Transaction>>
}

interface MerchantInventoryRepository {
    fun getInventory(merchantId: String): Flow<List<MerchantItem>>
    suspend fun upsertItem(item: MerchantItem): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    suspend fun updateStock(itemId: String, newStock: Int): Result<Unit>
}
