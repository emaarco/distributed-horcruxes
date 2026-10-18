package io.miragon.example.adapter.out.db.message

import com.fasterxml.jackson.databind.ObjectMapper
import io.miragon.example.adapter.process.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.adapter.process.ProcessVariables
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
        val variables = mapOf(ProcessVariables.MEMBERSHIP_ID to id.value.toString())
        val processMessage = toProcessMessage(MIRAVELO_REGISTRATION_SUBMITTED.value, id.value.toString(), variables)
        repository.save(processMessage)
    }

    override fun confirmMembership(id: MembershipId) {
        val variables = mapOf(ProcessVariables.MEMBERSHIP_ID to id.value.toString())
        val processMessage = toProcessMessage(MIRAVELO_MEMBERSHIP_CONFIRMED.value, id.value.toString(), variables)
        repository.save(processMessage)
        log.info { "Saved message for membership-confirmation $id" }
    }

    override fun rejectConfirmation(id: MembershipId) {
        val variables = mapOf(ProcessVariables.MEMBERSHIP_ID to id.value.toString())
        val processMessage = toProcessMessage(MIRAVELO_CONFIRMATION_REJECTED.value, id.value.toString(), variables)
        repository.save(processMessage)
        log.info { "Saved message for confirmation-rejection $id" }
    }

    private fun toProcessMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any>,
    ) = ProcessMessageEntity(
        messageName = messageName,
        correlationId = correlationId,
        variables = objectMapper.writeValueAsString(variables),
    )
}
