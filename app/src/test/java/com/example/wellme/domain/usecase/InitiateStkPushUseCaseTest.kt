package com.example.wellme.domain.usecase

import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.data.remote.model.StkPushResponse
import com.example.wellme.domain.repository.MpesaRepository
import com.example.wellme.util.Base64Encoder
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InitiateStkPushUseCaseTest {

    private lateinit var useCase: InitiateStkPushUseCase
    private val mpesaRepository: MpesaRepository = mock()
    private val base64Encoder: Base64Encoder = mock()

    @Before
    fun setup() {
        useCase = InitiateStkPushUseCase(mpesaRepository, base64Encoder)
    }

    @Test
    fun `invoke success calls repository with correct parameters`() = runTest {
        val consumerKey = "key"
        val consumerSecret = "secret"
        val businessShortCode = "174379"
        val passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
        val amount = "1"
        val phoneNumber = "254700000000"
        val callbackUrl = "https://example.com/callback"
        val accountReference = "TestAccount"
        val transactionDesc = "TestPayment"

        whenever(mpesaRepository.getAccessToken(any(), any())).doReturn(Result.success("test_token"))
        whenever(base64Encoder.encode(any())).doReturn("encoded_password")
        
        val expectedResponse = StkPushResponse("m_id", "c_id", "0", "Desc", "Msg")
        whenever(mpesaRepository.initiateStkPush(any(), any())).doReturn(Result.success(expectedResponse))

        val result = useCase(
            consumerKey, consumerSecret, businessShortCode, passkey, amount,
            phoneNumber, callbackUrl, accountReference, transactionDesc,
        )

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())

        // Verify token was fetched
        verify(mpesaRepository).getAccessToken(consumerKey, consumerSecret)

        // Verify STK push was initiated with correct data
        val requestCaptor = argumentCaptor<StkPushRequest>()
        verify(mpesaRepository).initiateStkPush(any(), requestCaptor.capture())
        
        val capturedRequest = requestCaptor.firstValue
        assertEquals(businessShortCode, capturedRequest.businessShortCode)
        assertEquals("encoded_password", capturedRequest.password)
        assertEquals(amount, capturedRequest.amount)
        assertEquals(phoneNumber, capturedRequest.phoneNumber)
        assertEquals(callbackUrl, capturedRequest.callBackUrl)
        
        // Verify timestamp format (yyyyMMddHHmmss is 14 characters)
        assertEquals(14, capturedRequest.timestamp.length)
        assertTrue(capturedRequest.timestamp.all { it.isDigit() })
    }

    @Test
    fun `invoke fails if token fetch fails`() = runTest {
        whenever(mpesaRepository.getAccessToken(any(), any())).doReturn(Result.failure(Exception("Token error")))

        val result = useCase(
            "k", "s", "123", "p", "1", "254", "url", "ref", "desc"
        )

        assertTrue(result.isFailure)
        assertEquals("Token error", result.exceptionOrNull()?.message)
    }
}
