package com.example.wellme.data.repository

import com.example.wellme.data.remote.MpesaApiService
import com.example.wellme.data.remote.model.MpesaTokenResponse
import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MpesaRepositoryImplTest {

    private lateinit var repository: MpesaRepositoryImpl
    private val apiService: MpesaApiService = mock()

    @Before
    fun setup() {
        repository = MpesaRepositoryImpl(apiService)
    }

    @Test
    fun `getAccessToken success returns token`() = runTest {
        val expectedToken = "test_token"
        whenever(apiService.getAccessToken(any(), any(), any())).doReturn(
            MpesaTokenResponse(expectedToken, "3599"),
        )

        val result = repository.getAccessToken("key", "secret")

        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
    }

    @Test
    fun `getAccessToken failure returns failure`() = runTest {
        whenever(apiService.getAccessToken(any(), any(), any())).thenThrow(RuntimeException("API Error"))

        val result = repository.getAccessToken("key", "secret")

        assertTrue(result.isFailure)
        assertEquals("API Error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `initiateStkPush success returns response`() = runTest {
        val expectedResponse = StkPushResponse(
            merchantRequestId = "123",
            checkoutRequestId = "456",
            responseCode = "0",
            responseDescription = "Success",
            customerMessage = "Success"
        )
        val request: StkPushRequest = mock()
        whenever(apiService.initiateStkPush(any(), any(), any())).doReturn(expectedResponse)

        val result = repository.initiateStkPush("token", request)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }
}
