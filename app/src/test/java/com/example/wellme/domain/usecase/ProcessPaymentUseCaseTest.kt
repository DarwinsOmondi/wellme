package com.example.wellme.domain.usecase

import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.WalletRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ProcessPaymentUseCaseTest {

    private lateinit var useCase: ProcessPaymentUseCase
    private val walletRepository: WalletRepository = mock()
    private val merchantRepository: MerchantRepository = mock()

    @Before
    fun setup() {
        useCase = ProcessPaymentUseCase(walletRepository, merchantRepository)
    }

    @Test
    fun `successful payment updates wallet and merchant pool`() = runTest {
        val studentId = "student1"
        val merchantId = "merchant1"
        val originalAmount = 1000L // 1000 cents
        
        val merchant = MerchantProfile(merchantId, "Shop", 0.1, 100000L, 5000L, true) // 10% discount
        val wallet = StudentWallet(studentId, 2000L, "ACTIVE") // 2000 cents balance

        whenever(merchantRepository.getMerchant(eq(merchantId), any())).thenReturn(flowOf(merchant))
        whenever(walletRepository.getWallet(eq(studentId), any())).thenReturn(flowOf(wallet))

        val result = useCase(studentId, merchantId, originalAmount)

        // 10% of 1000 is 100. Net is 900.
        assertEquals(900L, result.amountInCents)
        assertEquals(100L, result.discountAppliedInCents)

        verify(walletRepository).updateWalletBalance(studentId, 1100L) // 2000 - 900
        verify(merchantRepository).updateMerchantPool(merchantId, 5900L) // 5000 + 900
        verify(walletRepository).insertTransaction(any())
    }

    @Test
    fun `insufficient balance throws InsufficientBalanceException`() = runTest {
        val studentId = "student1"
        val merchantId = "merchant1"
        val originalAmount = 1000L 
        
        val merchant = MerchantProfile(merchantId, "Shop", 0.0, 100000L, 0L, true) // 0% discount
        val wallet = StudentWallet(studentId, 500L, "ACTIVE") // 500 cents balance

        whenever(merchantRepository.getMerchant(eq(merchantId), any())).thenReturn(flowOf(merchant))
        whenever(walletRepository.getWallet(eq(studentId), any())).thenReturn(flowOf(wallet))

        assertThrows(InsufficientBalanceException::class.java) {
            runTest { useCase(studentId, merchantId, originalAmount) }
        }
    }

    @Test
    fun `unregistered merchant throws IllegalEcosystemException`() = runTest {
        val merchantId = "unknown"
        whenever(merchantRepository.getMerchant(eq(merchantId), any())).thenReturn(flowOf(null))

        assertThrows(IllegalEcosystemException::class.java) {
            runTest { useCase("s1", merchantId, 1000L) }
        }
    }
}
