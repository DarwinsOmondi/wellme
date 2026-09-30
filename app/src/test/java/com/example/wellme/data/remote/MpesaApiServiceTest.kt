package com.example.wellme.data.remote

import com.example.wellme.data.remote.model.StkPushRequest
import com.example.wellme.util.Base64Encoder
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MpesaApiServiceTest {

    private val base64Encoder: Base64Encoder = mock()

    private fun createClient(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `getAccessToken calls correct endpoint and returns token`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials", request.url.toString())
            respond(
                content = """{"access_token": "mock_token", "expires_in": "3599"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        
        whenever(base64Encoder.encode(any())).doReturn("encoded_auth")
        
        val apiService = MpesaApiService(createClient(mockEngine), base64Encoder)
        val response = apiService.getAccessToken("key", "secret")

        assertEquals("mock_token", response.accessToken)
    }

    @Test
    fun `initiateStkPush calls correct endpoint and returns response`() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest", request.url.toString())
            respond(
                content = """{
                    "MerchantRequestID": "123",
                    "CheckoutRequestID": "456",
                    "ResponseCode": "0",
                    "ResponseDescription": "Success",
                    "CustomerMessage": "Success"
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val apiService = MpesaApiService(createClient(mockEngine), base64Encoder)
        val request = StkPushRequest(
            "123", "pass", "time", "type", "1", "254", "123", "254", "url", "ref", "desc"
        )
        val response = apiService.initiateStkPush("token", request)

        assertEquals("123", response.merchantRequestId)
        assertEquals("0", response.responseCode)
    }
}
