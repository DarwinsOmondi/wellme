package com.example.wellme.domain.usecase

import com.example.wellme.domain.model.CheckoutItem
import com.example.wellme.domain.model.CheckoutResult
import com.example.wellme.domain.repository.MerchantInventoryRepository
import com.example.wellme.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class IllegalEcosystemException(message: String) : Exception(message)
class InsufficientBalanceException(message: String) : Exception(message)

class ProcessPaymentUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val inventoryRepository: MerchantInventoryRepository
) {
    suspend operator fun invoke(
        items: List<CheckoutItem>,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): CheckoutResult {
        return walletRepository.checkoutWithWallet(items, idempotencyKey).getOrThrow()
    }

    suspend operator fun invoke(
        studentId: String,
        merchantId: String,
        originalAmountInCents: Long
    ): CheckoutResult {
        val items = inventoryRepository.getInventory(merchantId).firstOrNull() ?: emptyList()
        val pItems = if (items.isNotEmpty()) {
            listOf(CheckoutItem(itemId = items.first().id, quantity = 1))
        } else {
            emptyList()
        }
        val idempotencyKey = UUID.randomUUID().toString()
        return walletRepository.checkoutWithWallet(pItems, idempotencyKey).getOrThrow()
    }
}
