package com.example.wellme.data.repository

import com.example.wellme.data.remote.model.StkPushRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class MpesaRepositoryImplTest {

    private lateinit var repository: MpesaRepositoryImpl

    @Before
    fun setup() {
        repository = MpesaRepositoryImpl()
    }

    @Test
    fun `getAccessToken returns failure when client-side token generation is disabled`() = runTest {
        val result = repository.getAccessToken("key", "secret")
        assertTrue(result.isFailure)
    }

    @Test
    fun `initiateStkPush returns failure when direct client-side STK push is disabled`() = runTest {
        val request: StkPushRequest = mock()
        val result = repository.initiateStkPush("token", request)
        assertTrue(result.isFailure)
    }
}
