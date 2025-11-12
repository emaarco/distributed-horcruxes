package de.emaarco.example.application.service

import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Unit test for SendWelcomeMailService in idempotency-pattern.
 * Tests idempotency check and welcome mail sending logic.
 */
class SendWelcomeMailServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = SendWelcomeMailService(repository, processedOperationRepository)

    @Test
    fun `should send welcome mail when operation is not processed yet`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_SendWelcomeMail")
        val subscription = NewsletterSubscription(
            id = subscriptionId,
            email = Email("test@example.com"),
            name = Name("Test User"),
            newsletter = NewsletterId(UUID.randomUUID()),
            registrationDate = LocalDateTime.now(),
            status = SubscriptionStatus.CONFIRMED
        )

        every { processedOperationRepository.existsById(operationId) } returns false
        every { repository.find(subscriptionId) } returns subscription
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        underTest.sendWelcomeMail(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 1) { repository.find(subscriptionId) }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
        confirmVerified(repository, processedOperationRepository)
    }

    @Test
    fun `should skip sending welcome mail when operation is already processed`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_SendWelcomeMail")

        every { processedOperationRepository.existsById(operationId) } returns true

        // When
        underTest.sendWelcomeMail(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 0) { repository.find(any()) }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
        confirmVerified(repository, processedOperationRepository)
    }
}
