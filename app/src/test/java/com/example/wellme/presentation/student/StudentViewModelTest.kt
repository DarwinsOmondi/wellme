package com.example.wellme.presentation.student

import com.example.wellme.domain.model.MerchantProfile
import com.example.wellme.domain.model.StudentWallet
import com.example.wellme.domain.repository.AuthRepository
import com.example.wellme.domain.repository.KycRepository
import com.example.wellme.domain.repository.MerchantRepository
import com.example.wellme.domain.repository.WalletRepository
import com.example.wellme.domain.usecase.InitiateStkPushUseCase
import com.example.wellme.domain.usecase.ProcessPaymentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: StudentViewModel
    private val walletRepository: WalletRepository = mock()
    private val merchantRepository: MerchantRepository = mock()
    private val authRepository: AuthRepository = mock()
    private val kycRepository: KycRepository = mock()
    private val processPaymentUseCase: ProcessPaymentUseCase = mock()
    private val initiateStkPushUseCase: InitiateStkPushUseCase = mock()

    @Before
    fun setup() = runBlocking {
        Dispatchers.setMain(testDispatcher)
        
        whenever(walletRepository.getWallet(any(), any())).doReturn(flowOf(null))
        whenever(walletRepository.getTransactions()).doReturn(flowOf(emptyList()))
        whenever(kycRepository.getStudentKyc(any())).doReturn(Result.success(null))
        
        viewModel = StudentViewModel(
            walletRepository = walletRepository,
            merchantRepository = merchantRepository,
            authRepository = authRepository,
            kycRepository = kycRepository,
            processPaymentUseCase = processPaymentUseCase,
            initiateStkPushUseCase = initiateStkPushUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scanMerchant success updates sheetState to Scanned`() = runTest {
        val merchantId = "test_merchant"
        val mockMerchant = MerchantProfile(merchantId, "Campus Cafe", 0.15, 100000L, 0L, true)
        whenever(merchantRepository.getMerchant(any(), any())).doReturn(flowOf(mockMerchant))
        
        viewModel.scanMerchant(merchantId, 1000L)
        
        val state = viewModel.sheetState.value
        assertTrue(state is SheetState.Scanned)
        assertEquals(merchantId, (state as SheetState.Scanned).merchant.merchantId)
    }

    @Test
    fun `scanMerchant failure updates sheetState to Error`() = runTest {
        val merchantId = "unknown"
        whenever(merchantRepository.getMerchant(any(), any())).doReturn(flowOf(null))
        
        viewModel.scanMerchant(merchantId, 1000L)
        
        assertTrue(viewModel.sheetState.value is SheetState.Error)
    }
}
