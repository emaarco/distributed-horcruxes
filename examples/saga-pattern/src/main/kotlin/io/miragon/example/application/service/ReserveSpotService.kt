package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.ReserveSpotUseCase
import io.miragon.example.application.port.out.NewsletterSpotManager
import io.miragon.example.application.port.out.PayedNewsletterRepository
import io.miragon.example.domain.SubscriptionId
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
