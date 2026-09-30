package com.example.wellme.data.remote.model

import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("transaction_id") val transactionId: String,
    @SerialName("amount_in_cents") val amountInCents: Long,
    @SerialName("original_amount_in_cents") val originalAmountInCents: Long,
    @SerialName("discount_applied_in_cents") val discountAppliedInCents: Long,
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("type") val type: String,
    @SerialName("merchant_id") val merchantId: String?,
    @SerialName("student_id") val studentId: String?
) {
    fun toDomain() = Transaction(
        transactionId = transactionId,
        amountInCents = amountInCents,
        originalAmountInCents = originalAmountInCents,
        discountAppliedInCents = discountAppliedInCents,
        timestamp = timestamp,
        type = TransactionType.valueOf(type),
        merchantId = merchantId,
        studentId = studentId
    )

    companion object {
        fun fromDomain(domain: Transaction) = TransactionDto(
            transactionId = domain.transactionId,
            amountInCents = domain.amountInCents,
            originalAmountInCents = domain.originalAmountInCents,
            discountAppliedInCents = domain.discountAppliedInCents,
            timestamp = domain.timestamp,
            type = domain.type.name,
            merchantId = domain.merchantId,
            studentId = domain.studentId
        )
    }
}
