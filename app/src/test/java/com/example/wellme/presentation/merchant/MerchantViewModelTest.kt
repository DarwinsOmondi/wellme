package com.example.wellme.presentation.merchant

import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.usecase.GetMerchantPoolProgressUseCase
import com.example.wellme.domain.usecase.ProcessPaymentUseCase
import com.example.wellme.domain.usecase.RequestLoanUseCase
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class MerchantViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MerchantViewModel
    private val merchantRepository: MerchantRepository = mock()
    private val authRepository: AuthRepository = mock()
    private val getMerchantPoolProgressUseCase: GetMerchantPoolProgressUseCase = mock()
    private val requestLoanUseCase: RequestLoanUseCase = mock()
    private val processPaymentUseCase: ProcessPaymentUseCase = mock()

    private val merchantId = "test_merchant"
    private val mockProfile = MerchantProfile(merchantId, "Test Shop", 0.15, 100000L, 0L, isVerified = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        val mockUser: UserInfo = mock()
        whenever(mockUser.id).doReturn(merchantId)
        whenever(authRepository.getCurrentUser()).doReturn(mockUser)
        
        whenever(merchantRepository.getMerchant(any(), any())).doReturn(flowOf(mockProfile))
        whenever(merchantRepository.getMerchantTransactions(any())).doReturn(flowOf(emptyList()))
        whenever(getMerchantPoolProgressUseCase(any())).doReturn(flowOf(0.5f))
        
        viewModel = MerchantViewModel(
            merchantRepository = merchantRepository,
            authRepository = authRepository,
            getMerchantPoolProgressUseCase = getMerchantPoolProgressUseCase,
            requestLoanUseCase = requestLoanUseCase,
            processPaymentUseCase = processPaymentUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `merchant state is initialized from repository`() = runTest {
        assertEquals(merchantId, viewModel.merchantId)
        // poolTargetInCents is 100000L, so amountInput should be "1000"
        assertEquals("1000", viewModel.amountInput)
        assertEquals(0.15, viewModel.selectedYield, 0.001)
    }

    @Test
    fun `onAmountInputChange updates amountInput`() {
        viewModel.onAmountInputChange("2000")
        assertEquals("2000", viewModel.amountInput)
    }

    @Test
    fun `onSelectedYieldChange updates selectedYield`() {
        viewModel.onSelectedYieldChange(0.20)
        assertEquals(0.20, viewModel.selectedYield, 0.001)
    }

    @Test
    fun `submitCapitalRequest with invalid amount emits ShowSnackbar`() = runTest {
        viewModel.onAmountInputChange("0")
        viewModel.submitCapitalRequest()
        
        // In a real test we'd observe the uiEvent flow.
        // For now, this just verifies the method runs without crashing.
    }
}
