package com.example.wellme.domain.usecase

import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.WalletRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class IllegalEcosystemException(message: String) : Exception(message)
class InsufficientBalanceException(message: String) : Exception(message)

class ProcessPaymentUseCase(
    private val walletRepository: WalletRepository,
    private val merchantRepository: MerchantRepository
) {
    suspend operator fun invoke(
        studentId: String,
        merchantId: String,
        originalAmountInCents: Long
    ): Transaction {
        // 1. Verify merchant is registered (lock-in rule)
        val merchant = merchantRepository.getMerchant(merchantId, false).firstOrNull()
            ?: throw IllegalEcosystemException("Merchant $merchantId is not a registered food vendor in the WellMe ecosystem!")

        // 2. Fetch student wallet
        val wallet = walletRepository.getWallet(studentId, false).firstOrNull()
            ?: throw IllegalEcosystemException("Wallet not found for student $studentId")

        if (wallet.status != "ACTIVE") {
            throw IllegalEcosystemException("Student wallet is not active")
        }

        // 3. Calculate discount mathematically to prevent float inaccuracies
        val discountApplied = Math.round(originalAmountInCents * merchant.discountTier)
        val netAmount = originalAmountInCents - discountApplied

        // 4. Verify balance
        if (wallet.balanceInCents < netAmount) {
            throw InsufficientBalanceException("Insufficient balance! Required: ${netAmount / 100.0} KSh, Available: ${wallet.balanceInCents / 100.0} KSh")
        }

        // 5. Atomic-like DB updates
        val newWalletBalance = wallet.balanceInCents - netAmount
        walletRepository.updateWalletBalance(studentId, newWalletBalance)

        // 6. Update merchant pool raised amount
        val newPoolRaised = merchant.poolRaisedInCents + netAmount
        merchantRepository.updateMerchantPool(merchantId, newPoolRaised)

        // 7. Log transaction
        val transaction = Transaction(
            transactionId = UUID.randomUUID().toString(),
            amountInCents = netAmount,
            originalAmountInCents = originalAmountInCents,
            discountAppliedInCents = discountApplied,
            timestamp = System.currentTimeMillis(),
            type = TransactionType.PAYMENT,
            merchantId = merchantId,
            studentId = studentId
        )
        walletRepository.insertTransaction(transaction)

        return transaction
    }
}
