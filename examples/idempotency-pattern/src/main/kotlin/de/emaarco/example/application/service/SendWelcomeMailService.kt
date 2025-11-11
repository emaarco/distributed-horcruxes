package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.SendWelcomeMailUseCase
import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SendWelcomeMailService(
    private val repository: NewsletterSubscriptionRepository,
    private val processedOperationRepository: ProcessedOperationRepository
) : SendWelcomeMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendWelcomeMail(subscriptionId: SubscriptionId, operationId: OperationId) {
        if (processedOperationRepository.existsById(operationId.value)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        val subscription = repository.find(subscriptionId)
        log.info { "Sending welcome mail to ${subscription.email}" }

        processedOperationRepository.save(operationId.value)
    }
}
