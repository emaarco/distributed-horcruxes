package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.ReserveSpotUseCase
import de.emaarco.example.application.port.out.NewsletterSpotManager
import de.emaarco.example.application.port.out.PayedNewsletterRepository
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class ReserveSpotService(
    private val repository: PayedNewsletterRepository,
    private val spotManager: NewsletterSpotManager
) : ReserveSpotUseCase {

    private val log = KotlinLogging.logger {}

    override fun reserveSpot(subscriptionId: SubscriptionId) {
        val subscription = repository.find(subscriptionId)
        val reserved = spotManager.reserveSpot(subscription.email)

        if (!reserved) {
            throw IllegalStateException("No spots available for ${subscription.email.value}")
        }

        log.info { "Reserved spot for subscription ${subscriptionId.value}" }
    }
}
