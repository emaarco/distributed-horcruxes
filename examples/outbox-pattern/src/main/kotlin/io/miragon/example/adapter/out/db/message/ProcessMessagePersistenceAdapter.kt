package io.miragon.example.adapter.out.db.message

import com.fasterxml.jackson.databind.ObjectMapper
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Variables
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ProcessMessagePersistenceAdapter(
    private val repository: ProcessMessageJpaRepository,
) : MembershipProcess {

    private val log = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    override fun submitRegistration(id: MembershipId) {
        val processMessage = toProcessMessage(MIRAVELO_REGISTRATION_SUBMITTED.value, id)
        repository.save(processMessage)
        log.info { "Saved message for registration-submission $id" }
    }

    override fun confirmMembership(id: MembershipId) {
        val processMessage = toProcessMessage(MIRAVELO_MEMBERSHIP_CONFIRMED.value, id)
        repository.save(processMessage)
        log.info { "Saved message for membership-confirmation $id" }
    }

    override fun rejectConfirmation(id: MembershipId) {
        val processMessage = toProcessMessage(MIRAVELO_CONFIRMATION_REJECTED.value, id)
        repository.save(processMessage)
        log.info { "Saved message for confirmation-rejection $id" }
    }

    private fun toProcessMessage(messageName: String, id: MembershipId): ProcessMessageEntity {
        val variables = mapOf(Variables.StartEventRegistrationSubmitted.MEMBERSHIP_ID.value to id.value.toString())
        return ProcessMessageEntity(
            messageName = messageName,
            correlationId = id.value.toString(),
            variables = objectMapper.writeValueAsString(variables),
        )
    }
}
