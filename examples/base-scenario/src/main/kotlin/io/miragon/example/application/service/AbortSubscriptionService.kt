package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.AbortSubscriptionUseCase
import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class AbortSubscriptionService(
    private val repository: NewsletterSubscriptionRepository,
) : AbortSubscriptionUseCase {

    private val log = KotlinLogging.logger {}

    override fun abort(subscriptionId: SubscriptionId) {
        val subscription = repository.find(subscriptionId)
        subscription.abortRegistration()
        repository.save(subscription)
        log.info { "Aborted subscription-registration ${subscription.id}" }
    }
}
