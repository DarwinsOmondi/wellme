package com.example.wellme.di

import android.content.Context
import androidx.room.Room
import com.example.wellme.data.local.WellMeDatabase
import com.example.wellme.data.local.WalletDao
import com.example.wellme.data.local.MerchantDao
import com.example.wellme.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WellMeDatabase {
        return Room.databaseBuilder(
            context,
            WellMeDatabase::class.java,
            "wellme_database"
        )
            .fallbackToDestructiveMigration(false)
        .build()
    }

    @Provides
    fun provideWalletDao(database: WellMeDatabase): WalletDao {
        return database.walletDao()
    }

    @Provides
    fun provideMerchantDao(database: WellMeDatabase): MerchantDao {
        return database.merchantDao()
    }

    @Provides
    fun provideTransactionDao(database: WellMeDatabase): TransactionDao {
        return database.transactionDao()
    }
}
