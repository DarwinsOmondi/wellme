package com.example.wellme.domain.usecase

import com.example.wellme.domain.model.CheckoutItem
import com.example.wellme.domain.model.CheckoutResult
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.domain.repository.WalletRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ProcessPaymentUseCaseTest {

    private lateinit var useCase: ProcessPaymentUseCase
    private val walletRepository: WalletRepository = mock()
    private val inventoryRepository: MerchantInventoryRepository = mock()

    @Before
    fun setup() {
        useCase = ProcessPaymentUseCase(walletRepository, inventoryRepository)
    }

    @Test
    fun successfulCheckoutInvokesWalletRepositoryCheckoutWithWallet() = runTest {
        val items = listOf(CheckoutItem("item1", 2))
        val idempotencyKey = "idemp-123"
        val expectedResult = CheckoutResult(
            orderId = "order-1",
            status = "PAID",
            subtotalInCents = 1000L,
            discountInCents = 150L,
            totalInCents = 850L
        )

        whenever(walletRepository.checkoutWithWallet(eq(items), eq(idempotencyKey)))
            .thenReturn(Result.success(expectedResult))

        val result = useCase(items, idempotencyKey)

        assertEquals("order-1", result.orderId)
        assertEquals(850L, result.totalInCents)
        verify(walletRepository).checkoutWithWallet(items, idempotencyKey)
    }
}
