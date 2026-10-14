package com.example.wellme.data.repository

import android.util.Log
import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse
import com.example.wellme.domain.repository.MpesaRepository
import javax.inject.Inject

class MpesaRepositoryImpl @Inject constructor() : MpesaRepository {

    private val TAG = "MpesaRepositoryImpl"

    override suspend fun getAccessToken(consumerKey: String, consumerSecret: String): Result<String> {
        return Result.failure(Exception("Direct client-side token generation is disabled for security."))
    }

    override suspend fun initiateStkPush(accessToken: String, request: StkPushRequest): Result<StkPushResponse> {
        Log.w(TAG, "Attempted direct client-side Daraja STK Push - BLOCKED for security.")
        return Result.failure(Exception("Direct client-side STK Push is disabled. Wallet top-up must be processed via a trusted server-side endpoint."))
    }
}
