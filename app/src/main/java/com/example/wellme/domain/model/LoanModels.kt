package com.example.wellme.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MerchantLoanDto(
    @SerialName("id") val id: String,
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("amount_requested_in_cents") val amountRequestedInCents: Long,
    @SerialName("status") val status: String,
    @SerialName("safaricom_conversation_id") val conversationId: String? = null,
    @SerialName("created_at") val createdAt: String
)

sealed interface LoanLifecycleState {
    object Idle : LoanLifecycleState
    object SubmittingRpc : LoanLifecycleState
    data class RequestAccepted(val loanId: String, val conversationId: String?) : LoanLifecycleState
    data class DisbursedSuccess(val amountInCents: Long) : LoanLifecycleState
    data class OperationalError(val message: String) : LoanLifecycleState
}
