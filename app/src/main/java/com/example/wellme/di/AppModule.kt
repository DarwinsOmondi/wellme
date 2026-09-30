package com.example.wellme.di

import com.example.wellme.data.repository.MerchantInventoryRepositoryImpl
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.data.repository.WalletRepositoryImpl
import com.example.wellme.data.repository.MerchantRepositoryImpl
import com.example.wellme.data.repository.AuthRepositoryImpl
import com.example.wellme.data.repository.KycRepositoryImpl
import com.example.wellme.data.repository.MpesaRepositoryImpl
import com.example.wellme.domain.repository.WalletRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.domain.repository.MpesaRepository
import com.example.wellme.data.repository.LoanRepositoryImpl
import com.example.wellme.domain.repository.LoanRepository
import com.example.wellme.domain.usecase.ProcessPaymentUseCase
import com.example.wellme.domain.usecase.GetMerchantPoolProgressUseCase
import com.example.wellme.domain.usecase.InitiateStkPushUseCase
import com.example.wellme.util.AndroidBase64Encoder
import com.example.wellme.util.Base64Encoder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindMerchantRepository(impl: MerchantRepositoryImpl): MerchantRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindKycRepository(impl: KycRepositoryImpl): KycRepository

    @Binds
    @Singleton
    abstract fun bindMpesaRepository(impl: MpesaRepositoryImpl): MpesaRepository

    @Binds
    @Singleton
    abstract fun bindBase64Encoder(impl: AndroidBase64Encoder): Base64Encoder

    @Binds
    @Singleton
    abstract fun bindLoanRepository(impl: LoanRepositoryImpl): LoanRepository

    @Binds
    @Singleton
    abstract fun bindMerchantInventoryRepository(impl: MerchantInventoryRepositoryImpl): MerchantInventoryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object UsecaseModule {

    @Provides
    @Singleton
    fun provideProcessPaymentUseCase(
        walletRepository: WalletRepository,
        merchantRepository: MerchantRepository
    ): ProcessPaymentUseCase {
        return ProcessPaymentUseCase(walletRepository, merchantRepository)
    }

    @Provides
    @Singleton
    fun provideGetMerchantPoolProgressUseCase(
        merchantRepository: MerchantRepository
    ): GetMerchantPoolProgressUseCase {
        return GetMerchantPoolProgressUseCase(merchantRepository)
    }

    @Provides
    @Singleton
    fun provideInitiateStkPushUseCase(
        mpesaRepository: MpesaRepository,
        base64Encoder: Base64Encoder
    ): InitiateStkPushUseCase {
        return InitiateStkPushUseCase(mpesaRepository, base64Encoder)
    }
}
