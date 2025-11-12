package de.emaarco.example.application.service

import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Unit test for ConfirmSubscriptionService in idempotency-pattern.
 * Tests subscription confirmation and process notification logic.
 */
class ConfirmSubscriptionServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val processPort = mockk<NewsletterSubscriptionProcess>()
    private val underTest = ConfirmSubscriptionService(repository, processPort)

    @Test
    fun `should confirm subscription and notify process`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val subscription = NewsletterSubscription(
            id = subscriptionId,
            email = Email("test@example.com"),
            name = Name("Test User"),
            newsletter = NewsletterId(UUID.randomUUID()),
            registrationDate = LocalDateTime.now(),
            status = SubscriptionStatus.PENDING
        )

        every { repository.find(subscriptionId) } returns subscription
        every { repository.save(any<NewsletterSubscription>()) } returns mockk()
        every { processPort.confirmSubscription(subscriptionId) } just Runs

        // When
        underTest.confirm(subscriptionId)

        // Then
        verify(exactly = 1) { repository.find(subscriptionId) }
        verify(exactly = 1) { repository.save(any()) }
        verify(exactly = 1) { processPort.confirmSubscription(subscriptionId) }
        confirmVerified(repository, processPort)
    }
}
