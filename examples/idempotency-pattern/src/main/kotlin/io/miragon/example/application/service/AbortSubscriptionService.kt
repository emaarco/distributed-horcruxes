package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.AbortSubscriptionUseCase
import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
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
        if (processedOperationRepository.existsById(operationId)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        val subscription = repository.find(subscriptionId)
        subscription.abortRegistration()
        repository.save(subscription)
        log.info { "Aborted subscription-registration ${subscription.id}" }

        processedOperationRepository.save(operationId)
    }
}
