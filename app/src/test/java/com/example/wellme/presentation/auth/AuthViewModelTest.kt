package com.example.wellme.presentation.auth

import com.example.wellme.domain.repository.AuthRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AuthViewModel
    private val repository: AuthRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn success updates state to OtpSent`() = runTest {
        whenever(repository.signIn(any())).doReturn(Result.success(Unit))
        
        viewModel.email = "test@example.com"
        
        viewModel.signIn()
        
        assertEquals(AuthState.OtpSent, viewModel.state.value)
    }

    @Test
    fun `signIn failure updates state to Error`() = runTest {
        whenever(repository.signIn(any())).doReturn(Result.failure(Exception("Invalid email")))
        
        viewModel.email = "fail@example.com"
        
        viewModel.signIn()
        
        val state = viewModel.state.value
        assert(state is AuthState.Error)
        assertEquals("Invalid email", (state as AuthState.Error).message)
    }

    @Test
    fun `signUp success updates state to OtpSent`() = runTest {
        whenever(repository.signUp(any(), any())).doReturn(Result.success(Unit))
        
        viewModel.email = "test@example.com"
        viewModel.role = "STUDENT"
        
        viewModel.signUp()
        
        assertEquals(AuthState.OtpSent, viewModel.state.value)
    }
}
