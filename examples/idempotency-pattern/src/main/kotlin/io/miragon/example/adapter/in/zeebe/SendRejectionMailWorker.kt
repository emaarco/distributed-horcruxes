package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.camunda.client.api.response.ActivatedJob
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.ServiceTasks
import io.miragon.example.application.port.`in`.SendRejectionMailUseCase
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendRejectionMailWorker(private val useCase: SendRejectionMailUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_SEND_REJECTION_MAIL)
    fun sendRejectionMail(
        job: ActivatedJob,
        @Variable("membershipId") membershipId: String
    ) {
        log.debug { "Received Zeebe job to send rejection mail: $membershipId" }
        useCase.sendRejectionMail(
            MembershipId(UUID.fromString(membershipId)),
            OperationId("$membershipId-${job.elementId}")
        )
    }
}
