package io.miragon.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Variables
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.domain.MembershipId
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Base scenario: direct process engine calls without transaction safety.
 * Every call happens immediately, potentially before the database commit.
 */
@Component
class MembershipProcessAdapter(
    private val camundaClient: CamundaClient
) : MembershipProcess {

    override fun submitRegistration(id: MembershipId) {
        val variables = mapOf(Variables.StartEventRegistrationSubmitted.MEMBERSHIP_ID.value to id.value.toString())
        camundaClient.newPublishMessageCommand()
            .messageName(MIRAVELO_REGISTRATION_SUBMITTED.value)
            .withoutCorrelationKey()
            .variables(variables)
            .send()
            .join()
    }

    override fun confirmMembership(id: MembershipId) {
        publishCorrelated(MIRAVELO_MEMBERSHIP_CONFIRMED.value, id)
    }

    override fun rejectConfirmation(id: MembershipId) {
        publishCorrelated(MIRAVELO_CONFIRMATION_REJECTED.value, id)
    }

    private fun publishCorrelated(messageName: String, id: MembershipId) {
        camundaClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(id.value.toString())
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }
}
