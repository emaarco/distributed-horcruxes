package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.ConfirmSubscriptionUseCase
import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class ConfirmSubscriptionService(
    private val repository: NewsletterSubscriptionRepository,
    private val processPort: NewsletterSubscriptionProcess
) : ConfirmSubscriptionUseCase {

    private val log = KotlinLogging.logger {}

    override fun confirm(subscriptionId: SubscriptionId) {
        val subscription = repository.find(subscriptionId)
        subscription.confirmRegistration()
        repository.save(subscription)
        processPort.confirmSubscription(subscription.id)
        log.info { "Confirmed subscription ${subscription.id}" }
    }
}
