package com.example.wellme.domain.repository

import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse

interface MpesaRepository {
    suspend fun getAccessToken(consumerKey: String, consumerSecret: String): Result<String>
    suspend fun initiateStkPush(accessToken: String, request: StkPushRequest): Result<StkPushResponse>
}
