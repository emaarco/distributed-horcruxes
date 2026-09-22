package io.miragon.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.response.PublishMessageResponse
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.domain.MembershipId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class MembershipProcessAdapterTest {

    private val camundaClient = mockk<CamundaClient>(relaxed = true)
    private val underTest = MembershipProcessAdapter(camundaClient)

    @Test
    fun `should publish registration submitted message when submitting registration`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MIRAVELO_REGISTRATION_SUBMITTED.value)
                .withoutCorrelationKey()
                .variables(any<Map<String, Any>>())
                .send()
                .join()
        } returns mockk<PublishMessageResponse>()

        // When
        underTest.submitRegistration(membershipId)

        // Then
        verify(exactly = 1) {
            camundaClient.newPublishMessageCommand()
                .messageName(MIRAVELO_REGISTRATION_SUBMITTED.value)
                .withoutCorrelationKey()
                .variables(any<Map<String, Any>>())
                .send()
                .join()
        }
    }

    @Test
    fun `should publish membership confirmed message when confirming membership`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MIRAVELO_MEMBERSHIP_CONFIRMED.value)
                .correlationKey(membershipId.value.toString())
                .timeToLive(any())
                .send()
                .join()
        } returns mockk<PublishMessageResponse>()

        // When
        underTest.confirmMembership(membershipId)

        // Then
        verify(exactly = 1) {
            camundaClient.newPublishMessageCommand()
                .messageName(MIRAVELO_MEMBERSHIP_CONFIRMED.value)
                .correlationKey(membershipId.value.toString())
                .timeToLive(any())
                .send()
                .join()
        }
    }

    @Test
    fun `should publish confirmation rejected message when rejecting confirmation`() {
        // Given
        val membershipId = MembershipId(UUID.randomUUID())
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MIRAVELO_CONFIRMATION_REJECTED.value)
                .correlationKey(membershipId.value.toString())
                .timeToLive(any())
                .send()
                .join()
        } returns mockk<PublishMessageResponse>()

        // When
        underTest.rejectConfirmation(membershipId)

        // Then
        verify(exactly = 1) {
            camundaClient.newPublishMessageCommand()
                .messageName(MIRAVELO_CONFIRMATION_REJECTED.value)
                .correlationKey(membershipId.value.toString())
                .timeToLive(any())
                .send()
                .join()
        }
    }
}
