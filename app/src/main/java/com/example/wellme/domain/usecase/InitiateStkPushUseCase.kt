package com.example.wellme.domain.usecase

import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse
import com.example.wellme.domain.repository.MpesaRepository
import com.example.wellme.util.Base64Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class InitiateStkPushUseCase @Inject constructor(
    private val mpesaRepository: MpesaRepository,
    private val base64Encoder: Base64Encoder
) {
    suspend operator fun invoke(
        consumerKey: String,
        consumerSecret: String,
        businessShortCode: String,
        passkey: String,
        amount: String,
        phoneNumber: String,
        callbackUrl: String,
        accountReference: String,
        transactionDesc: String
    ): Result<StkPushResponse> {
        return try {
            // 1. Get Access Token
            val tokenResult = mpesaRepository.getAccessToken(consumerKey, consumerSecret)
            if (tokenResult.isFailure) return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Failed to get token"))
            val accessToken = tokenResult.getOrThrow()

            // 2. Prepare Timestamp (yyyyMMddHHmmss)
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

            // 3. Generate Password (Base64 encoded Shortcode + Passkey + Timestamp)
            val passwordSource = "$businessShortCode$passkey$timestamp"
            val password = base64Encoder.encode(passwordSource.toByteArray())

            // 4. Create Request
            val request = StkPushRequest(
                businessShortCode = businessShortCode,
                password = password,
                timestamp = timestamp,
                transactionType = "CustomerPayBillOnline",
                amount = amount,
                partyA = phoneNumber,
                partyB = businessShortCode,
                phoneNumber = phoneNumber,
                callBackUrl = callbackUrl,
                accountReference = accountReference,
                transactionDesc = transactionDesc
            )

            // 5. Initiate STK Push
            mpesaRepository.initiateStkPush(accessToken, request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
