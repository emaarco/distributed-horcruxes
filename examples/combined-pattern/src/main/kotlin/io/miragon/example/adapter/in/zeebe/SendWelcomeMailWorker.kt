package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.camunda.client.api.response.ActivatedJob
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.ServiceTasks
import io.miragon.example.application.port.`in`.SendWelcomeMailUseCase
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendWelcomeMailWorker(private val useCase: SendWelcomeMailUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_SEND_WELCOME_MAIL)
    fun sendWelcomeMail(job: ActivatedJob, @Variable("membershipId") membershipId: String) {
        log.debug { "Received Zeebe job to send welcome mail: $membershipId" }
        useCase.sendWelcomeMail(
            MembershipId(UUID.fromString(membershipId)),
            OperationId("$membershipId-${job.elementId}")
        )
    }
}
