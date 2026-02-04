package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.adapter.process.PayedNewsletterSubscriptionProcessApi.TaskTypes
import io.miragon.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Variables
import io.miragon.example.application.port.`in`.ProcessPaymentUseCase
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class ProcessPaymentWorker(
    private val useCase: ProcessPaymentUseCase
) {
    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_SEND_CONFIRMATION_MAIL)
    fun processPayment(
        @Variable(Variables.SUBSCRIPTION_ID) subscriptionId: String
    ): Map<String, Boolean> {
        log.debug { "Received Zeebe job to process payment: $subscriptionId" }
        val paymentSuccessful = useCase.processPayment(SubscriptionId(UUID.fromString(subscriptionId)))
        return mapOf("paymentSuccessful" to paymentSuccessful)
    }
}
