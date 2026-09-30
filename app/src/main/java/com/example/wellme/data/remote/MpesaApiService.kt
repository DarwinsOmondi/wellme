package com.example.wellme.data.remote

import android.util.Log
import com.example.wellme.data.remote.model.MpesaTokenResponse
import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse
import com.example.wellme.util.Base64Encoder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class MpesaApiService @Inject constructor(
    private val client: HttpClient,
    private val base64Encoder: Base64Encoder
) {
    private val TAG = "MpesaApiService"
    private val sandboxBaseUrl = "https://sandbox.safaricom.co.ke"
    private val productionBaseUrl = "https://api.safaricom.co.ke"

    private fun getBaseUrl(isSandbox: Boolean) = if (isSandbox) sandboxBaseUrl else productionBaseUrl

    suspend fun getAccessToken(
        consumerKey: String,
        consumerSecret: String,
        isSandbox: Boolean = true
    ): MpesaTokenResponse {
        val baseUrl = getBaseUrl(isSandbox)
        Log.d(TAG, "getAccessToken calling: $baseUrl/oauth/v1/generate")
        val authString = base64Encoder.encode("$consumerKey:$consumerSecret".toByteArray())
        return client.get("$baseUrl/oauth/v1/generate?grant_type=client_credentials") {
            header("Authorization", "Basic $authString")
        }.body()
    }

    suspend fun initiateStkPush(
        accessToken: String,
        request: StkPushRequest,
        isSandbox: Boolean = true
    ): StkPushResponse {
        val baseUrl = getBaseUrl(isSandbox)
        Log.d(TAG, "initiateStkPush calling: $baseUrl/mpesa/stkpush/v1/processrequest")
        return client.post("$baseUrl/mpesa/stkpush/v1/processrequest") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
