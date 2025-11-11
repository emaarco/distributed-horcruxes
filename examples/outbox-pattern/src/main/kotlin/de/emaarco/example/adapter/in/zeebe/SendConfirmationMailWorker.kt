package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.application.port.`in`.SendConfirmationMailUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendConfirmationMailWorker(private val useCase: SendConfirmationMailUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.Activity_SendConfirmationMail)
    fun sendConfirmationMail(@Variable("subscriptionId") subscriptionId: String) {
        log.debug { "Received Zeebe job to send confirmation mail: $subscriptionId" }
        useCase.sendConfirmationMail(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}