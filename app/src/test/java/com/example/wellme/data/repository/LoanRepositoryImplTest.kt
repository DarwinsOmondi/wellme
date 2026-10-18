package com.example.wellme.data.repository

import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class LoanRepositoryImplTest {

    private val postgrest: Postgrest = mock()
    private val realtime: Realtime = mock()
    private val functions: Functions = mock()

    private var mockStatus = 200
    private var mockBody = ""
    private var shouldThrow = false
    private var capturedPayload: JsonObject? = null

    private lateinit var repository: LoanRepositoryImpl

    @Before
    fun setup() {
        repository = object : LoanRepositoryImpl(postgrest, realtime, functions) {
            override suspend fun invokeLoanRequestFunction(jsonObject: JsonObject): Pair<Int, String> {
                capturedPayload = jsonObject
                if (shouldThrow) throw RuntimeException("Network failure")
                return Pair(mockStatus, mockBody)
            }
        }
        shouldThrow = false
        capturedPayload = null
    }

    @Test
    fun `requestLoan success returns loanId and payload does not contain merchantId`() = runTest {
        mockStatus = 200
        mockBody = """{"loan_id":"loan_uuid_123","success":true}"""

        val result = repository.requestLoan(100000L, 10.0)
        assertTrue(result.isSuccess)
        assertEquals("loan_uuid_123", result.getOrNull())

        // Verify request payload does NOT contain merchantId or secret credentials
        val payload = capturedPayload
        assertTrue(payload != null)
        assertTrue(!payload!!.containsKey("merchant_id"))
        assertTrue(!payload.containsKey("p_merchant_id"))
        assertTrue(!payload.containsKey("service_role"))
        assertTrue(!payload.containsKey("secret"))
        assertEquals("100000", payload["amount_requested_in_cents"]?.toString())
    }

    @Test
    fun `requestLoan 401 unauthorized returns failure`() = runTest {
        mockStatus = 401
        mockBody = """{"error":"Unauthorized"}"""

        val result = repository.requestLoan(100000L, 10.0)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("session expired", ignoreCase = true) == true)
    }

    @Test
    fun `requestLoan 409 conflict returns failure`() = runTest {
        mockStatus = 409
        mockBody = """{"error":"Already submitted"}"""

        val result = repository.requestLoan(100000L, 10.0)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already submitted", ignoreCase = true) == true)
    }

    @Test
    fun `requestLoan 422 liquidity error returns failure`() = runTest {
        mockStatus = 422
        mockBody = """{"message":"Insufficient till liquidity"}"""

        val result = repository.requestLoan(100000L, 10.0)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Insufficient till liquidity") == true)
    }

    @Test
    fun `requestLoan 502 gateway error returns failure`() = runTest {
        mockStatus = 502
        mockBody = """{"error":"Bad Gateway"}"""

        val result = repository.requestLoan(100000L, 10.0)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("refresh", ignoreCase = true) == true)
    }

    @Test
    fun `requestLoan network exception returns failure without retry`() = runTest {
        shouldThrow = true

        val result = repository.requestLoan(100000L, 10.0)
        assertTrue(result.isFailure)
    }
}
