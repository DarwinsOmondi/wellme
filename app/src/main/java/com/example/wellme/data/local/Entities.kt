package com.example.wellme.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.model.Transaction
import com.example.wellme.domain.model.TransactionType

@Entity(tableName = "student_wallets")
data class StudentWalletEntity(
    @PrimaryKey val studentId: String,
    val balanceInCents: Long,
    val status: String
) {
    fun toDomain() = StudentWallet(
        studentId = studentId,
        balanceInCents = balanceInCents,
        status = status
    )

    companion object {
        fun fromDomain(domain: StudentWallet) = StudentWalletEntity(
            studentId = domain.studentId,
            balanceInCents = domain.balanceInCents,
            status = domain.status
        )
    }
}

@Entity(tableName = "merchants")
data class MerchantEntity(
    @PrimaryKey val merchantId: String,
    val businessName: String,
    val discountTier: Double,
    val poolTargetInCents: Long,
    val poolRaisedInCents: Long,
    val isVerified: Boolean
) {
    fun toDomain() = MerchantProfile(
        merchantId = merchantId,
        businessName = businessName,
        discountTier = discountTier,
        poolTargetInCents = poolTargetInCents,
        poolRaisedInCents = poolRaisedInCents,
        isVerified = isVerified
    )

    companion object {
        fun fromDomain(domain: MerchantProfile) = MerchantEntity(
            merchantId = domain.merchantId,
            businessName = domain.businessName,
            discountTier = domain.discountTier,
            poolTargetInCents = domain.poolTargetInCents,
            poolRaisedInCents = domain.poolRaisedInCents,
            isVerified = domain.isVerified
        )
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val amountInCents: Long,
    val originalAmountInCents: Long,
    val discountAppliedInCents: Long,
    val timestamp: Long,
    val type: String,
    val merchantId: String?,
    val studentId: String?
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
        fun fromDomain(domain: Transaction) = TransactionEntity(
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
