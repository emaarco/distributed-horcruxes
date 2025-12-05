package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.adapter.process.PayedNewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Variables
import de.emaarco.example.application.port.`in`.CancelReservationUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class CancelReservationWorker(
    private val useCase: CancelReservationUseCase
) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_CANCEL_SPOT)
    fun cancelReservation(
        @Variable(Variables.SUBSCRIPTION_ID) subscriptionId: String
    ) {
        log.debug { "Received task to cancel reservation: $subscriptionId" }
        useCase.cancelReservation(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}
