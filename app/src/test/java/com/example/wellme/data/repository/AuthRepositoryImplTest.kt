package com.example.wellme.data.repository

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AuthRepositoryImplTest {

    private lateinit var repository: AuthRepositoryImpl
    private val auth: Auth = mock()

    @Before
    fun setup() {
        repository = AuthRepositoryImpl(auth)
    }

    @Test
    fun `getCurrentUser returns user from auth`() {
        val mockUser: UserInfo = mock()
        whenever(auth.currentUserOrNull()).thenReturn(mockUser)

        val result = repository.getCurrentUser()

        assertEquals(mockUser, result)
    }

    @Test
    fun `signOut success returns success`() = runTest {
        val result = repository.signOut()
        verify(auth).signOut()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `signOut failure returns failure`() = runTest {
        whenever(auth.signOut()).thenThrow(RuntimeException("Error"))
        val result = repository.signOut()
        assertTrue(result.isFailure)
        assertEquals("Error", result.exceptionOrNull()?.message)
    }
}
