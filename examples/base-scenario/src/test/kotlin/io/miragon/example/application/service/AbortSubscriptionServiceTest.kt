package io.miragon.example.application.service

import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class AbortSubscriptionServiceTest {

    private val repository = mockk<NewsletterSubscriptionRepository>()
    private val underTest = AbortSubscriptionService(repository)

    @Test
    fun `should abort subscription registration`() {
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

        // When
        underTest.abort(subscriptionId)

        // Then
        verify(exactly = 1) { repository.find(subscriptionId) }
        verify(exactly = 1) { repository.save(any()) }
        confirmVerified(repository)
    }
}
