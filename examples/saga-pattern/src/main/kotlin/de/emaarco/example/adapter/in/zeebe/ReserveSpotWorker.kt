package de.emaarco.example.adapter.`in`.zeebe

import de.emaarco.example.adapter.process.PayedNewsletterSubscriptionProcessApi.TaskTypes
import de.emaarco.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Variables
import de.emaarco.example.application.port.`in`.ReserveSpotUseCase
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class ReserveSpotWorker(
    private val useCase: ReserveSpotUseCase
) {
    private val log = KotlinLogging.logger {}

    @JobWorker(type = TaskTypes.NEWSLETTER_SEND_RESERVE_SPOT)
    fun reserveSpot(
        @Variable(Variables.SUBSCRIPTION_ID) subscriptionId: String
    ) {
        log.debug { "Received Zeebe job to reserve spot: $subscriptionId" }
        useCase.reserveSpot(SubscriptionId(UUID.fromString(subscriptionId)))
    }
}
