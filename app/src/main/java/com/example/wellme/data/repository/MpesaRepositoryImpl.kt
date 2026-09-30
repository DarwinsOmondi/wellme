package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.remote.MpesaApiService
import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse
import com.example.wellme.domain.repository.MpesaRepository
import javax.inject.Inject

class MpesaRepositoryImpl @Inject constructor(
    private val apiService: MpesaApiService
) : MpesaRepository {

    private val TAG = "MpesaRepositoryImpl"

    override suspend fun getAccessToken(consumerKey: String, consumerSecret: String): Result<String> {
        Log.d(TAG, "Requesting M-Pesa Access Token")
        return try {
            val response = apiService.getAccessToken(consumerKey, consumerSecret)
            Log.d(TAG, "M-Pesa Access Token received successfully")
            Result.success(response.accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching M-Pesa Access Token", e)
            Result.failure(e)
        }
    }

    override suspend fun initiateStkPush(accessToken: String, request: StkPushRequest): Result<StkPushResponse> {
        Log.d(TAG, "Initiating M-Pesa STK Push for: ${request.phoneNumber}, Amount: ${request.amount}")
        return try {
            val response = apiService.initiateStkPush(accessToken, request)
            Log.d(TAG, "STK Push initiated successfully. CheckoutRequestID: ${response.checkoutRequestId}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating M-Pesa STK Push", e)
            Result.failure(e)
        }
    }
}
