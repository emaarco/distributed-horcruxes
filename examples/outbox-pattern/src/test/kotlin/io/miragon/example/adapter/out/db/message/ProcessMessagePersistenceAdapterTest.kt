package io.miragon.example.adapter.out.db.message

import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.domain.MembershipId
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ProcessMessagePersistenceAdapterTest {

    private val repository = mockk<ProcessMessageJpaRepository>()
    private val underTest = ProcessMessagePersistenceAdapter(repository)
    private val membershipId = MembershipId(UUID.randomUUID())
    private val savedMessage = slot<ProcessMessageEntity>()

    @Test
    fun `should save pending registration submitted message`() {
        // Given
        every { repository.save(capture(savedMessage)) } answers { firstArg() }

        // When
        underTest.submitRegistration(membershipId)

        // Then
        assertSavedMessage(MIRAVELO_REGISTRATION_SUBMITTED.value)
    }

    @Test
    fun `should save pending membership confirmed message`() {
        // Given
        every { repository.save(capture(savedMessage)) } answers { firstArg() }

        // When
        underTest.confirmMembership(membershipId)

        // Then
        assertSavedMessage(MIRAVELO_MEMBERSHIP_CONFIRMED.value)
    }

    @Test
    fun `should save pending confirmation rejected message`() {
        // Given
        every { repository.save(capture(savedMessage)) } answers { firstArg() }

        // When
        underTest.rejectConfirmation(membershipId)

        // Then
        assertSavedMessage(MIRAVELO_CONFIRMATION_REJECTED.value)
    }

    private fun assertSavedMessage(expectedMessageName: String) {
        val message = savedMessage.captured
        assertEquals(expectedMessageName, message.messageName)
        assertEquals(membershipId.value.toString(), message.correlationId)
        assertEquals("""{"membershipId":"${membershipId.value}"}""", message.variables)
        assertEquals(MessageStatus.PENDING, message.status)
    }
}
