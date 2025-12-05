package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.CancelReservationUseCase
import de.emaarco.example.application.port.out.NewsletterSpotManager
import de.emaarco.example.application.port.out.PayedNewsletterRepository
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class CancelReservationService(
    private val repository: PayedNewsletterRepository,
    private val spotManager: NewsletterSpotManager
) : CancelReservationUseCase {

    private val log = KotlinLogging.logger {}

    override fun cancelReservation(subscriptionId: SubscriptionId) {
        val subscription = repository.find(subscriptionId)
        spotManager.releaseSpot(subscription.email)
        log.warn { "Cancelled reservation for subscription ${subscriptionId.value}" }
    }
}
