package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.AbortSubscriptionUseCase
import de.emaarco.example.application.port.out.NewsletterSubscriptionRepository
import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class AbortSubscriptionService(
    private val repository: NewsletterSubscriptionRepository,
    private val processedOperationRepository: ProcessedOperationRepository
) : AbortSubscriptionUseCase {

    private val log = KotlinLogging.logger {}

    override fun abort(subscriptionId: SubscriptionId, operationId: OperationId) {
        if (processedOperationRepository.existsById(operationId.value)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        val subscription = repository.find(subscriptionId)
        subscription.abortRegistration()
        repository.save(subscription)
        log.info { "Aborted subscription-registration ${subscription.id}" }

        processedOperationRepository.save(operationId.value)
    }
}
