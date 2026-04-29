package io.miragon.example.application.service

import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.application.port.out.SubscriptionCounterRepository
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionCounter
import io.miragon.example.domain.SubscriptionId
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class IncrementSubscriptionCounterServiceTest {

    private val counterRepository = mockk<SubscriptionCounterRepository>()
    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = IncrementSubscriptionCounterService(counterRepository, processedOperationRepository)

    @Test
    fun `should increment counter when operation is not processed yet`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_RegistrationCompleted")
        val counter = SubscriptionCounter(count = 5)

        every { processedOperationRepository.existsById(operationId) } returns false
        every { counterRepository.find() } returns counter
        every { counterRepository.save(any()) } just Runs
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        underTest.incrementCounter(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 1) { counterRepository.find() }
        verify(exactly = 1) { counterRepository.save(SubscriptionCounter(count = 6)) }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
        confirmVerified(counterRepository, processedOperationRepository)
    }

    @Test
    fun `should skip incrementing counter when operation is already processed`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_RegistrationCompleted")

        every { processedOperationRepository.existsById(operationId) } returns true

        // When
        underTest.incrementCounter(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 0) { counterRepository.find() }
        verify(exactly = 0) { counterRepository.save(any()) }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
        confirmVerified(counterRepository, processedOperationRepository)
    }
}
