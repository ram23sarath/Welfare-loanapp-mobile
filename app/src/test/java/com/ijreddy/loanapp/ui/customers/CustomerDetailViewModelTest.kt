package com.ijreddy.loanapp.ui.customers

import androidx.lifecycle.SavedStateHandle
import com.ijreddy.loanapp.data.local.entity.CustomerEntity
import com.ijreddy.loanapp.data.local.entity.DataEntryEntity
import com.ijreddy.loanapp.data.local.entity.LoanEntity
import com.ijreddy.loanapp.data.local.entity.SubscriptionEntity
import com.ijreddy.loanapp.data.repository.CustomerRepository
import com.ijreddy.loanapp.data.repository.DataEntryRepository
import com.ijreddy.loanapp.data.repository.LoanRepository
import com.ijreddy.loanapp.data.repository.SubscriptionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class CustomerDetailViewModelTest {

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
    fun `customer detail state aggregates totals`() = runTest {
        val customerRepository = mockk<CustomerRepository>()
        val loanRepository = mockk<LoanRepository>()
        val subscriptionRepository = mockk<SubscriptionRepository>()
        val dataEntryRepository = mockk<DataEntryRepository>()

        val customer = CustomerEntity("c1", "Priya", "9999999999", Instant.now().toString())

        coEvery { customerRepository.getById("c1") } returns customer
        every { loanRepository.getByCustomerId("c1") } returns flowOf(
            listOf(
                LoanEntity("l1", "c1", 10000.0, 2.0, "2024-01-01", 12, 900.0, Instant.now().toString())
            )
        )
        every { subscriptionRepository.getByCustomerId("c1") } returns flowOf(
            listOf(
                SubscriptionEntity("s1", "c1", 1500.0, "2024-01-01", created_at = Instant.now().toString())
            )
        )
        every { dataEntryRepository.getByCustomerId("c1") } returns flowOf(
            listOf(
                DataEntryEntity(
                    id = "d1",
                    customer_id = "c1",
                    type = "credit",
                    amount = 500.0,
                    description = "bonus",
                    date = "2024-02-01",
                    created_at = Instant.now().toString()
                )
            )
        )

        val viewModel = CustomerDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("customerId" to "c1")),
            customerRepository = customerRepository,
            loanRepository = loanRepository,
            subscriptionRepository = subscriptionRepository,
            dataEntryRepository = dataEntryRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Priya", state.customer?.name)
        assertEquals("9999999999@loanapp.local", state.customerEmail)
        assertEquals(1, state.loans.size)
        assertEquals(1, state.subscriptions.size)
        assertEquals(1, state.entries.size)
    }
}
