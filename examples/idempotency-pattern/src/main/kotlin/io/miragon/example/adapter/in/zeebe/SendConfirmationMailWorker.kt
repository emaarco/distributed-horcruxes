package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.camunda.client.api.response.ActivatedJob
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.ServiceTasks
import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailWorker(private val useCase: SendConfirmationMailUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_SEND_CONFIRMATION_MAIL)
    fun sendConfirmationMail(
        job: ActivatedJob,
        @Variable("membershipId") membershipId: String
    ) {
        log.debug { "Received Zeebe job to send confirmation mail: $membershipId" }
        useCase.sendConfirmationMail(
            MembershipId(UUID.fromString(membershipId)),
            OperationId("$membershipId-${job.elementId}")
        )
    }
}
