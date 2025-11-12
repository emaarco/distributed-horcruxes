package de.emaarco.example.application.service

import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Unit test for SendConfirmationMailService in idempotency-pattern.
 * Tests idempotency check and confirmation mail sending logic.
 */
class SendConfirmationMailServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = SendConfirmationMailService(repository, processedOperationRepository)

    @Test
    fun `should send confirmation mail when operation is not processed yet`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_SendConfirmationMail")
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
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        underTest.sendConfirmationMail(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 1) { repository.find(subscriptionId) }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
        confirmVerified(repository, processedOperationRepository)
    }

    @Test
    fun `should skip sending confirmation mail when operation is already processed`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val operationId = OperationId("${subscriptionId.value}-Activity_SendConfirmationMail")

        every { processedOperationRepository.existsById(operationId) } returns true

        // When
        underTest.sendConfirmationMail(subscriptionId, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 0) { repository.find(any()) }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
        confirmVerified(repository, processedOperationRepository)
    }
}
