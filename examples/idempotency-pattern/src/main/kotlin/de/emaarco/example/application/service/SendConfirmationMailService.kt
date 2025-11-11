package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.SendConfirmationMailUseCase
import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SendConfirmationMailService(
    private val repository: NewsletterSubscriptionRepository,
    private val processedOperationRepository: ProcessedOperationRepository
) : SendConfirmationMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendConfirmationMail(subscriptionId: SubscriptionId, operationId: OperationId) {
        if (processedOperationRepository.existsById(operationId)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        val subscription = repository.find(subscriptionId)
        log.info { "Sending confirmation mail to ${subscription.email}" }

        processedOperationRepository.save(operationId)
    }
}
