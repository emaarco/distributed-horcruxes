package io.miragon.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import io.miragon.example.adapter.process.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.adapter.process.ProcessVariables
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.domain.MembershipId
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class MembershipProcessAdapter(
    private val camundaClient: CamundaClient
) : MembershipProcess {

    override fun submitRegistration(id: MembershipId) {
        val variables = mapOf(ProcessVariables.MEMBERSHIP_ID to id.value.toString())
        publishCorrelated(MIRAVELO_REGISTRATION_SUBMITTED.value, id, variables)
    }

    override fun confirmMembership(id: MembershipId) {
        publishCorrelated(MIRAVELO_MEMBERSHIP_CONFIRMED.value, id)
    }

    override fun rejectConfirmation(id: MembershipId) {
        publishCorrelated(MIRAVELO_CONFIRMATION_REJECTED.value, id)
    }

    private fun publishCorrelated(messageName: String, id: MembershipId, variables: Map<String, Any> = emptyMap()) {
        camundaClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(id.value.toString())
            .messageId("${id.value}-$messageName")
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }
}
