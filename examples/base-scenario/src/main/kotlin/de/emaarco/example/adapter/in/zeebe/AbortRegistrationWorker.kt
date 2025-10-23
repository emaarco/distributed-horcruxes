package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.application.port.`in`.AbortSubscriptionUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class AbortRegistrationWorker(private val useCase: AbortSubscriptionUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = "newsletter.abortRegistration")
    fun abortRegistration(@Variable("subscriptionId") subscriptionId: String) {
        log.debug { "Received Zeebe job to abort registration: $subscriptionId" }
        useCase.abort(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}