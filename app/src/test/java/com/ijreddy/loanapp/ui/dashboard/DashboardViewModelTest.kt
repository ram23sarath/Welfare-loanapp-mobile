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
                LoanEntity("l1", "c1", 50000.0, 2.0, "2024-01-01", 12, 4500.0, Instant.now().toString()),
                LoanEntity("l2", "c2", 20000.0, 1.5, "2024-02-01", 6, 3500.0, Instant.now().toString())
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

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.adminState.value.activeCustomers)
        assertEquals(2, viewModel.adminState.value.totalLoans)
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
                LoanEntity("l1", "c1", 50000.0, 2.0, "2024-01-01", 12, 4500.0, Instant.now().toString()),
                LoanEntity("l2", "c2", 20000.0, 1.5, "2024-02-01", 6, 3500.0, Instant.now().toString())
            )
        )
        every { subscriptionRepository.subscriptions } returns MutableStateFlow(
            listOf(
                SubscriptionEntity("s1", "c1", 2000.0, "2024-01-01", created_at = Instant.now().toString()),
                SubscriptionEntity("s2", "c2", 1500.0, "2024-01-01", created_at = Instant.now().toString())
            )
        )

        val viewModel = DashboardViewModel(
            authRepository = authRepository,
            customerRepository = customerRepository,
            loanRepository = loanRepository,
            subscriptionRepository = subscriptionRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.customerState.value.activeLoans)
        assertEquals(1, viewModel.customerState.value.activeSubscriptions)
    }
}
