package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SubscribeToPayedNewsletterUseCase
import io.miragon.example.application.port.out.NewsletterSpotManager
import io.miragon.example.application.port.out.PayedNewsletterRepository
import io.miragon.example.application.port.out.PayedNewsletterSubscriptionProcess
import io.miragon.example.domain.PayedNewsletterSubscription
import io.miragon.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SubscribeToPayedNewsletterService(
    private val repository: PayedNewsletterRepository,
    private val processPort: PayedNewsletterSubscriptionProcess,
    private val spotManager: NewsletterSpotManager
) : SubscribeToPayedNewsletterUseCase {

    private val log = KotlinLogging.logger {}

    override fun subscribe(command: SubscribeToPayedNewsletterUseCase.Command): SubscriptionId {
        ensureThatSpotsAreAvailable()
        val subscription = buildSubscription(command)
        repository.save(subscription)
        processPort.submitForm(subscription.id)
        log.info { "Subscribed ${command.email.value} to payed newsletter" }
        return subscription.id
    }

    private fun ensureThatSpotsAreAvailable() {
        if (!spotManager.hasAvailableSpots()) {
            throw IllegalStateException("No available spots. Newsletter subscription is full.")
        }
    }

    private fun buildSubscription(command: SubscribeToPayedNewsletterUseCase.Command) = PayedNewsletterSubscription(
        email = command.email,
        name = command.name
    )
}
