package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.application.port.`in`.AbortSubscriptionUseCase
import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId
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

    @JobWorker(type = "newsletter.abortRegistration")
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
