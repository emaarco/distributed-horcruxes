package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.SendWelcomeMailUseCase
import de.emaarco.example.application.port.out.PayedNewsletterRepository
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SendWelcomeMailService(
    private val repository: PayedNewsletterRepository,
) : SendWelcomeMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendWelcomeMail(subscriptionId: SubscriptionId) {
        val subscription = repository.find(subscriptionId)
        log.info { "Sending welcome mail to ${subscription.email.value}" }
    }
}
