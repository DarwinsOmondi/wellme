package com.example.wellme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudentWalletEntity::class,
        MerchantEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WellMeDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun merchantDao(): MerchantDao
    abstract fun transactionDao(): TransactionDao
}
