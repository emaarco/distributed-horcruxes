package de.emaarco.example.application.service

import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Unit test for AbortSubscriptionService in idempotency-pattern.
 * Tests idempotency check and subscription abort logic.
 */
class AbortSubscriptionServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = AbortSubscriptionService(repository, processedOperationRepository)

    @Test
    fun `should abort subscription when operation is not processed yet`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_AbortRegistration")
        val subscription = NewsletterSubscription(
            id = subscriptionId,
            email = Email("test@example.com"),
            name = Name("Test User"),
            newsletter = NewsletterId(UUID.randomUUID()),
            registrationDate = LocalDateTime.now(),
            status = SubscriptionStatus.PENDING
        )

        every { processedOperationRepository.existsById(operationId) } returns false
        every { repository.find(subscriptionId) } returns subscription
        every { repository.save(any<NewsletterSubscription>()) } returns mockk()
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        underTest.abort(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 1) { repository.find(subscriptionId) }
        verify(exactly = 1) { repository.save(any()) }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
        confirmVerified(repository, processedOperationRepository)
    }

    @Test
    fun `should skip aborting subscription when operation is already processed`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_AbortRegistration")

        every { processedOperationRepository.existsById(operationId) } returns true

        // When
        underTest.abort(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 0) { repository.find(any()) }
        verify(exactly = 0) { repository.save(any<NewsletterSubscription>()) }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
        confirmVerified(repository, processedOperationRepository)
    }
}
