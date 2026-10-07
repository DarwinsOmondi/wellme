package com.example.wellme.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MpesaTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: String
)

@Serializable
data class StkPushRequest(
    @SerialName("BusinessShortCode") val businessShortCode: String,
    @SerialName("Password") val password: String,
    @SerialName("Timestamp") val timestamp: String,
    @SerialName("TransactionType") val transactionType: String,
    @SerialName("Amount") val amount: String,
    @SerialName("PartyA") val partyA: String,
    @SerialName("PartyB") val partyB: String,
    @SerialName("PhoneNumber") val phoneNumber: String,
    @SerialName("CallBackURL") val callBackUrl: String,
    @SerialName("AccountReference") val accountReference: String,
    @SerialName("TransactionDesc") val transactionDesc: String
)

@Serializable
data class StkPushResponse(
    @SerialName("MerchantRequestID") val merchantRequestId: String,
    @SerialName("CheckoutRequestID") val checkoutRequestId: String,
    @SerialName("ResponseCode") val responseCode: String,
    @SerialName("ResponseDescription") val responseDescription: String,
    @SerialName("CustomerMessage") val customerMessage: String
)

@Serializable
data class B2cRequest(
    @SerialName("InitiatorName") val initiatorName: String,
    @SerialName("SecurityCredential") val securityCredential: String,
    @SerialName("CommandID") val commandID: String,
    @SerialName("Amount") val amount: String,
    @SerialName("PartyA") val partyA: String,
    @SerialName("PartyB") val partyB: String,
    @SerialName("Remarks") val remarks: String,
    @SerialName("QueueTimeOutURL") val queueTimeOutURL: String,
    @SerialName("ResultURL") val resultURL: String,
    @SerialName("Occasion") val occasion: String
)

@Serializable
data class B2cResponse(
    @SerialName("ConversationID") val conversationID: String,
    @SerialName("OriginatorConversationID") val originatorConversationID: String,
    @SerialName("ResponseCode") val responseCode: String,
    @SerialName("ResponseDescription") val responseDescription: String
)

@Serializable
data class PendingPaymentDto(
    @SerialName("checkout_request_id") val checkoutRequestId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("amount_in_cents") val amountInCents: Long,
    @SerialName("status") val status: String
) {
    fun toDomain() = com.example.wellme.domain.model.PendingPayment(
        checkoutRequestId = checkoutRequestId,
        studentId = studentId,
        amountInCents = amountInCents,
        status = status,
        timestamp = System.currentTimeMillis()
    )

    companion object {
        fun fromDomain(domain: com.example.wellme.domain.model.PendingPayment) = PendingPaymentDto(
            checkoutRequestId = domain.checkoutRequestId,
            studentId = domain.studentId,
            amountInCents = domain.amountInCents,
            status = domain.status
        )
    }
}
