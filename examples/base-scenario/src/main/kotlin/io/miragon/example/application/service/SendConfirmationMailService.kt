package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.SubscriptionId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SendConfirmationMailService(
    private val repository: NewsletterSubscriptionRepository,
) : SendConfirmationMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendConfirmationMail(subscriptionId: SubscriptionId) {
        val subscription = repository.find(subscriptionId)
        log.info { "Sending confirmation mail to ${subscription.email}" }
    }
}