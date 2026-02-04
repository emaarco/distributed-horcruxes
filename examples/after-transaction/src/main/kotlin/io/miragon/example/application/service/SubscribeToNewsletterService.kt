package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SubscribeToNewsletterUseCase
import io.miragon.example.application.port.out.NewsletterSubscriptionProcess
import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.NewsletterSubscription
import io.miragon.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SubscribeToNewsletterService(
    private val repository: NewsletterSubscriptionRepository,
    private val processPort: NewsletterSubscriptionProcess
) : SubscribeToNewsletterUseCase {

    private val log = KotlinLogging.logger {}

    override fun subscribe(command: SubscribeToNewsletterUseCase.Command): SubscriptionId {
        val subscription = buildSubscription(command)
        repository.save(subscription)
        processPort.submitForm(subscription.id)
        log.info { "Subscribed ${command.email} to newsletter ${command.newsletterId}" }
        return subscription.id
    }

    private fun buildSubscription(command: SubscribeToNewsletterUseCase.Command) = NewsletterSubscription(
        email = command.email,
        name = command.name,
        newsletter = command.newsletterId
    )
}