package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.`in`.IncrementSubscriptionCounterUseCase
import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.camunda.client.api.response.ActivatedJob
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class RegistrationCompletedWorker(
    private val useCase: IncrementSubscriptionCounterUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_REGISTRATION_COMPLETED)
    fun handleRegistrationCompleted(
        job: ActivatedJob,
        @Variable("subscriptionId") subscriptionId: String
    ) {
        log.debug { "Received Zeebe job for registration completed: $subscriptionId" }
        useCase.incrementCounter(
            subscriptionId = SubscriptionId(UUID.fromString(subscriptionId)),
            operationId = OperationId("$subscriptionId-${job.elementId}")
        )
    }
}
