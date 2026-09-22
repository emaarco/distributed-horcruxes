package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Variables
import io.miragon.example.domain.MembershipId
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class MembershipProcessAdapterTest {

    private val engineApi = mockk<ProcessEngineApi>()
    private val underTest = MembershipProcessAdapter(engineApi)

    @Test
    fun `should start process via message when submitting registration`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        val expectedVariables = mapOf(
            Variables.StartEventRegistrationSubmitted.MEMBERSHIP_ID.value to membershipId.value.toString()
        )

        every { engineApi.startProcessViaMessage(any(), any(), any()) } returns mockk()

        // When
        underTest.submitRegistration(membershipId)

        // Then
        verify(exactly = 1) {
            engineApi.startProcessViaMessage(
                messageName = MIRAVELO_REGISTRATION_SUBMITTED.value,
                correlationId = membershipId.value.toString(),
                variables = expectedVariables
            )
        }
        confirmVerified(engineApi)
    }

    @Test
    fun `should send message when confirming membership`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())

        every { engineApi.sendMessage(any(), any(), any()) } returns mockk()

        // When
        underTest.confirmMembership(membershipId)

        // Then
        verify(exactly = 1) {
            engineApi.sendMessage(
                messageName = MIRAVELO_MEMBERSHIP_CONFIRMED.value,
                correlationId = membershipId.value.toString()
            )
        }
        confirmVerified(engineApi)
    }

    @Test
    fun `should send message when rejecting confirmation`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())

        every { engineApi.sendMessage(any(), any(), any()) } returns mockk()

        // When
        underTest.rejectConfirmation(membershipId)

        // Then
        verify(exactly = 1) {
            engineApi.sendMessage(
                messageName = MIRAVELO_CONFIRMATION_REJECTED.value,
                correlationId = membershipId.value.toString()
            )
        }
        confirmVerified(engineApi)
    }
}
