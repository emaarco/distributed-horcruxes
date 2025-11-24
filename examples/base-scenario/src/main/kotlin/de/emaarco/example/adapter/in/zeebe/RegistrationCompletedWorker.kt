package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.`in`.IncrementSubscriptionCounterUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

/**
 * Worker that demonstrates the idempotency problem with non-idempotent operations.
 *
 * This worker listens to the newsletter.registrationCompleted message end event and increments
 * a subscription counter. To simulate real-world failures, it randomly throws an exception
 * AFTER incrementing the counter but BEFORE acknowledging job completion to Zeebe.
 *
 * This causes Zeebe to retry the job, leading to multiple increments for the same registration
 * completion - demonstrating why increment operations are not idempotent and require proper
 * handling in distributed systems.
 *
 * See the idempotency-pattern example for the solution using an operation log.
 */
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
            throw RuntimeException("Simulating error on acknowledging, leading to idempotency problem")
        }
    }
}