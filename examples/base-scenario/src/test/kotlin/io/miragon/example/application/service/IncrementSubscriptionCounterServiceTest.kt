package io.miragon.example.application.service

import io.miragon.example.application.port.out.SubscriptionCounterRepository
import io.miragon.example.domain.SubscriptionCounter
import io.miragon.example.domain.SubscriptionId
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class IncrementSubscriptionCounterServiceTest {

    private val counterRepository = mockk<SubscriptionCounterRepository>()
    private val underTest = IncrementSubscriptionCounterService(counterRepository)

    @Test
    fun `should increment subscription counter`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val counter = SubscriptionCounter(count = 3)

        every { counterRepository.find() } returns counter
        every { counterRepository.save(any()) } just Runs

        // When
        underTest.incrementCounter(subscriptionId)

        // Then
        verify(exactly = 1) { counterRepository.find() }
        verify(exactly = 1) { counterRepository.save(SubscriptionCounter(count = 4)) }
        confirmVerified(counterRepository)
    }
}
