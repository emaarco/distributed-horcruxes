package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.adapter.process.PayedNewsletterSubscriptionProcessApi.TaskTypes
import io.miragon.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Variables
import io.miragon.example.application.port.`in`.SendWelcomeMailUseCase
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class SendWelcomeMailWorker(
    private val useCase: SendWelcomeMailUseCase
) {
    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_SEND_WELCOME_MAIL)
    fun sendWelcomeMail(
        @Variable(Variables.SUBSCRIPTION_ID) subscriptionId: String
    ) {
        log.debug { "Received Zeebe job to send welcome mail: $subscriptionId" }
        useCase.sendWelcomeMail(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}
