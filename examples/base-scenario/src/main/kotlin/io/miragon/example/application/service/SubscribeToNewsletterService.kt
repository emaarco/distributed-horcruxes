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

    /**
     * BASE SCENARIO - Demonstrates the distributed transaction problem:
     *
     * 1. Save to DB (uncommitted)
     * 2. Notify Zeebe immediately - process starts, worker tries to load the subscription
     * 3. PROBLEM: Worker may not see data yet because transaction isn't committed!
     * 4. Sleep simulates slow processing before commit
     * 5. Transaction commits when the method returns (too late!)
     */
    override fun subscribe(command: SubscribeToNewsletterUseCase.Command): SubscriptionId {
        val subscription = buildSubscription(command)
        repository.save(subscription)  // Uncommitted
        processPort.submitForm(subscription.id)  // Zeebe starts immediately (PROBLEM!)
        log.info { "Subscribed ${command.email} to newsletter ${command.newsletterId}" }
        Thread.sleep(250)  // Simulate slow commit
        return subscription.id  // Transaction commits here
    }

    private fun buildSubscription(command: SubscribeToNewsletterUseCase.Command) = NewsletterSubscription(
        email = command.email,
        name = command.name,
        newsletter = command.newsletterId
    )
}