package com.example.wellme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM student_wallets WHERE studentId = :studentId LIMIT 1")
    fun getWalletFlow(studentId: String): Flow<StudentWalletEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: StudentWalletEntity)

    @Query("UPDATE student_wallets SET balanceInCents = :newBalance WHERE studentId = :studentId")
    suspend fun updateWalletBalance(studentId: String, newBalance: Long)
}

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchants WHERE merchantId = :merchantId LIMIT 1")
    fun getMerchantFlow(merchantId: String): Flow<MerchantEntity?>

    @Query("SELECT * FROM merchants")
    fun getAllMerchantsFlow(): Flow<List<MerchantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: MerchantEntity)

    @Query("UPDATE merchants SET poolRaisedInCents = :raisedAmount WHERE merchantId = :merchantId")
    suspend fun updateMerchantPool(merchantId: String, raisedAmount: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE merchantId = :merchantId ORDER BY timestamp DESC")
    fun getMerchantTransactionsFlow(merchantId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}
