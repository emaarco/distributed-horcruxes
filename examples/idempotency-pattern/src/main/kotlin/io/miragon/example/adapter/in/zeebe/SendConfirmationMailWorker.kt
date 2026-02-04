package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.camunda.client.api.response.ActivatedJob
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailWorker(
    private val useCase: SendConfirmationMailUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_SEND_CONFIRMATION_MAIL)
    fun sendConfirmationMail(
        job: ActivatedJob,
        @Variable("subscriptionId") subscriptionId: String
    ) {
        log.debug { "Received Zeebe job to send confirmation mail: $subscriptionId" }
        useCase.sendConfirmationMail(
            SubscriptionId(UUID.fromString(subscriptionId)),
            OperationId("$subscriptionId-${job.elementId}")
        )
    }
}
