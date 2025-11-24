package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.`in`.IncrementSubscriptionCounterUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class RegistrationCompletedWorker(
    private val useCase: IncrementSubscriptionCounterUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.END_EVENT_REGISTRATION_COMPLETED)
    fun handleRegistrationCompleted(@Variable("subscriptionId") subscriptionId: String) {
        log.debug { "Received Zeebe job for registration completed: $subscriptionId" }
        useCase.incrementCounter(SubscriptionId(UUID.fromString(subscriptionId)))
        if (Math.random() > 0.8) {
            RuntimeException("Simulating error on acknowledging, leading to idempotency problem")
        }
    }
}