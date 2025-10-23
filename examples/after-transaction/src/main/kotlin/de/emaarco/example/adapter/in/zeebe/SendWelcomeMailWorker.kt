package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.application.port.`in`.SendWelcomeMailUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.zeebe.spring.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendWelcomeMailWorker(private val useCase: SendWelcomeMailUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = "newsletter.sendWelcomeMail")
    fun sendConfirmationMail(@Variable("subscriptionId") subscriptionId: String) {
        log.debug { "Received Zeebe job to send welcome mail: $subscriptionId" }
        useCase.sendWelcomeMail(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}