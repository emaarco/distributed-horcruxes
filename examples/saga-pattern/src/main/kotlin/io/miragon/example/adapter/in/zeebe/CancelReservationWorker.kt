package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.adapter.process.PayedNewsletterSubscriptionProcessApi.TaskTypes
import io.miragon.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Variables
import io.miragon.example.application.port.`in`.CancelReservationUseCase
import io.miragon.example.domain.SubscriptionId
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
