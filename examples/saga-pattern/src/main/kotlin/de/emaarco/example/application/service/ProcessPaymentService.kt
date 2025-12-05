package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.ProcessPaymentUseCase
import de.emaarco.example.application.port.out.PayedNewsletterRepository
import de.emaarco.example.domain.PayedNewsletterSubscription
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
@Transactional
class ProcessPaymentService(
    private val repository: PayedNewsletterRepository
) : ProcessPaymentUseCase {

    private val log = KotlinLogging.logger {}

    override fun processPayment(subscriptionId: SubscriptionId): Boolean {
        val subscription = repository.find(subscriptionId)
        val paymentSuccessful = Random.nextBoolean()
        val updated = subscription.processPayment(paymentSuccessful)
        repository.save(updated)
        updated.notifyAboutResult()
        return paymentSuccessful
    }

    private fun PayedNewsletterSubscription.notifyAboutResult() = when (paymentSuccessful) {
        true -> log.info { "Payment successful for subscription ${id.value}" }
        null -> log.warn { "Payment not yet confirmed for subscription ${id.value}" }
        else -> log.warn { "Payment failed for subscription ${id.value}" }
    }

}
