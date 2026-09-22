package io.miragon.example.adapter.out.db.message

import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.domain.MembershipId
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
    fun `should store a pending registration-submitted message in the outbox`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        val captured = slot<ProcessMessageEntity>()
        every { repository.save(capture(captured)) } answers { captured.captured }

        // When
        underTest.submitRegistration(membershipId)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        assertEquals(MIRAVELO_REGISTRATION_SUBMITTED.value, captured.captured.messageName)
        assertEquals(membershipId.value.toString(), captured.captured.correlationId)
        assertEquals(MessageStatus.PENDING, captured.captured.status)
        assertTrue(captured.captured.variables.contains(membershipId.value.toString()))
    }

    @Test
    fun `should store a pending membership-confirmed message in the outbox`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        val captured = slot<ProcessMessageEntity>()
        every { repository.save(capture(captured)) } answers { captured.captured }

        // When
        underTest.confirmMembership(membershipId)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        assertEquals(MIRAVELO_MEMBERSHIP_CONFIRMED.value, captured.captured.messageName)
        assertEquals(membershipId.value.toString(), captured.captured.correlationId)
        assertEquals(MessageStatus.PENDING, captured.captured.status)
    }

    @Test
    fun `should store a pending confirmation-rejected message in the outbox`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        val captured = slot<ProcessMessageEntity>()
        every { repository.save(capture(captured)) } answers { captured.captured }

        // When
        underTest.rejectConfirmation(membershipId)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        assertEquals(MIRAVELO_CONFIRMATION_REJECTED.value, captured.captured.messageName)
        assertEquals(membershipId.value.toString(), captured.captured.correlationId)
        assertEquals(MessageStatus.PENDING, captured.captured.status)
    }
}
