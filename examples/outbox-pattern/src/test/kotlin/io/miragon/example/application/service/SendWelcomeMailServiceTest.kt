package io.miragon.example.application.service

import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class SendWelcomeMailServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val underTest = SendWelcomeMailService(repository)

    @Test
    fun `should find subscription and send welcome mail`() {
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

        // When
        underTest.sendWelcomeMail(subscriptionId)

        // Then
        verify(exactly = 1) { repository.find(subscriptionId) }
        confirmVerified(repository)
    }
}
