package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SubscribeToNewsletterUseCase
import io.miragon.example.application.port.out.NewsletterSubscriptionProcess
import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for SubscribeToNewsletterService in idempotency-pattern.
 * Tests subscription creation and process notification logic.
 */
class SubscribeToNewsletterServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val processPort = mockk<NewsletterSubscriptionProcess>()
    private val underTest = SubscribeToNewsletterService(repository, processPort)

    @Test
    fun `should create subscription and notify process when subscribing to newsletter`() {
        // Given
        val command = SubscribeToNewsletterUseCase.Command(
            email = Email("test@example.com"),
            name = Name("Test User"),
            newsletterId = NewsletterId(UUID.randomUUID())
        )

        every { repository.save(any<NewsletterSubscription>()) } returns mockk()
        every { processPort.submitForm(any()) } just Runs

        // When
        val subscriptionId = underTest.subscribe(command)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        verify(exactly = 1) { processPort.submitForm(subscriptionId) }
        confirmVerified(repository, processPort)
    }
}
