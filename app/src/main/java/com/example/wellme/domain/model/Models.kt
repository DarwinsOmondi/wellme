package com.example.wellme.domain.model

enum class TransactionType {
    PAYMENT,     // Wallet deduction for food
    STIPEND,     // Wallet addition of stipend
    FUNDING      // Merchant receiving direct cash
}

data class StudentWallet(
    val studentId: String,
    val balanceInCents: Long,
    val status: String = "ACTIVE"
)

data class MerchantProfile(
    val merchantId: String,
    val businessName: String,
    val discountTier: Double, // e.g. 0.15 for 15%
    val poolTargetInCents: Long,
    val poolRaisedInCents: Long,
    val isVerified: Boolean = true
)

data class Transaction(
    val transactionId: String,
    val amountInCents: Long, // Net amount processed
    val originalAmountInCents: Long, // Original cost before discount
    val discountAppliedInCents: Long, // Amount saved
    val timestamp: Long,
    val type: TransactionType,
    val merchantId: String?,
    val studentId: String? // Truncated/masked in UI for merchant view
)

data class PendingPayment(
    val checkoutRequestId: String,
    val studentId: String,
    val amountInCents: Long,
    val status: String, // PENDING, COMPLETED, FAILED
    val timestamp: Long
)
