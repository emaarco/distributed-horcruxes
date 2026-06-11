package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.ServiceTasks
import io.miragon.example.application.port.`in`.AbortSubscriptionUseCase
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.camunda.client.api.response.ActivatedJob
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class AbortSubscriptionWorker(
    private val useCase: AbortSubscriptionUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.NEWSLETTER_ABORT_REGISTRATION)
    fun abortRegistration(
        job: ActivatedJob,
        @Variable("subscriptionId") subscriptionId: String
    ) {
        log.debug { "Received Zeebe job to abort subscription: $subscriptionId" }
        useCase.abort(
            SubscriptionId(UUID.fromString(subscriptionId)),
            OperationId("$subscriptionId-${job.elementId}")
        )
    }
}
