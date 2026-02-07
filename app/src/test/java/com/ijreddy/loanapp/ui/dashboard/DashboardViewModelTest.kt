package com.ijreddy.loanapp.ui.dashboard

import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import com.ijreddy.loanapp.data.repository.AuthRepository
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.repository.SubscriptionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `admin state reflects active customers and loans`() = runTest {
        val customerRepository = mockk<CustomerRepository>()
        val loanRepository = mockk<LoanRepository>()
        val subscriptionRepository = mockk<SubscriptionRepository>()
        val authRepository = mockk<AuthRepository>()

        every { customerRepository.customers } returns MutableStateFlow(
            listOf(
                CustomerEntity("c1", "Priya", "9999999999", Instant.now().toString()),
                CustomerEntity("c2", "Ravi", "8888888888", Instant.now().toString())
            )
        )
        every { loanRepository.loans } returns MutableStateFlow(
            listOf(
                LoanEntity(
                    id = "l1",
                    customer_id = "c1",
                    original_amount = 50000.0,
                    interest_amount = 1000.0,
                    payment_date = "2024-01-01",
                    total_instalments = 12,
                    created_at = Instant.now().toString()
                ),
                LoanEntity(
                    id = "l2",
                    customer_id = "c2",
                    original_amount = 20000.0,
                    interest_amount = 300.0,
                    payment_date = "2024-02-01",
                    total_instalments = 6,
                    created_at = Instant.now().toString()
                )
            )
        )
        every { subscriptionRepository.subscriptions } returns MutableStateFlow(emptyList())
        every { authRepository.scopedCustomerId } returns MutableStateFlow(null)

        val viewModel = DashboardViewModel(
            authRepository = authRepository,
            customerRepository = customerRepository,
            loanRepository = loanRepository,
            subscriptionRepository = subscriptionRepository
        )

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.adminState.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.adminState.value.activeCustomers)
        assertEquals(2, viewModel.adminState.value.totalLoans)
        
        collectJob.cancel()
    }

    @Test
    fun `customer state filters scoped customer data`() = runTest {
        val customerRepository = mockk<CustomerRepository>()
        val loanRepository = mockk<LoanRepository>()
        val subscriptionRepository = mockk<SubscriptionRepository>()
        val authRepository = mockk<AuthRepository>()

        every { authRepository.scopedCustomerId } returns MutableStateFlow("c1")
        every { customerRepository.customers } returns MutableStateFlow(emptyList())
        every { loanRepository.loans } returns MutableStateFlow(
            listOf(
                LoanEntity(
                    id = "l1",
                    customer_id = "c1",
                    original_amount = 50000.0,
                    interest_amount = 1000.0,
                    payment_date = "2024-01-01",
                    total_instalments = 12,
                    created_at = Instant.now().toString()
                ),
                LoanEntity(
                    id = "l2",
                    customer_id = "c2",
                    original_amount = 20000.0,
                    interest_amount = 300.0,
                    payment_date = "2024-02-01",
                    total_instalments = 6,
                    created_at = Instant.now().toString()
                )
            )
        )
        every { subscriptionRepository.subscriptions } returns MutableStateFlow(
            listOf(
                SubscriptionEntity(
                    id = "s1",
                    customer_id = "c1",
                    amount = 2000.0,
                    date = "2024-01-01",
                    created_at = Instant.now().toString()
                ),
                SubscriptionEntity(
                    id = "s2",
                    customer_id = "c2",
                    amount = 1500.0,
                    date = "2024-01-01",
                    created_at = Instant.now().toString()
                )
            )
        )

        val viewModel = DashboardViewModel(
            authRepository = authRepository,
            customerRepository = customerRepository,
            loanRepository = loanRepository,
            subscriptionRepository = subscriptionRepository
        )

        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.customerState.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.customerState.value.activeLoans)
        assertEquals(1, viewModel.customerState.value.activeSubscriptions)
        
        collectJob.cancel()
    }
}
