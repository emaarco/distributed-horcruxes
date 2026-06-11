package io.miragon.example.adapter.out.db.message

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
import io.miragon.example.domain.SubscriptionId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for ProcessMessagePersistenceAdapter in combined-pattern.
 * Verifies that process messages are written to the outbox table as PENDING rows.
 */
class ProcessMessagePersistenceAdapterTest {

    private val repository = mockk<ProcessMessageJpaRepository>(relaxed = true)
    private val underTest = ProcessMessagePersistenceAdapter(repository)

    @Test
    fun `should store a pending form-submitted message in the outbox`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val captured = slot<ProcessMessageEntity>()
        every { repository.save(capture(captured)) } answers { captured.captured }

        // When
        underTest.submitForm(subscriptionId)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        assertEquals(MESSAGE_FORM_SUBMITTED.value, captured.captured.messageName)
        assertEquals(subscriptionId.value.toString(), captured.captured.correlationId)
        assertEquals(MessageStatus.PENDING, captured.captured.status)
        assertTrue(captured.captured.variables.contains(subscriptionId.value.toString()))
    }

    @Test
    fun `should store a pending subscription-confirmed message in the outbox`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val captured = slot<ProcessMessageEntity>()
        every { repository.save(capture(captured)) } answers { captured.captured }

        // When
        underTest.confirmSubscription(subscriptionId)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        assertEquals(MESSAGE_SUBSCRIPTION_CONFIRMED.value, captured.captured.messageName)
        assertEquals(subscriptionId.value.toString(), captured.captured.correlationId)
        assertEquals(MessageStatus.PENDING, captured.captured.status)
    }
}
