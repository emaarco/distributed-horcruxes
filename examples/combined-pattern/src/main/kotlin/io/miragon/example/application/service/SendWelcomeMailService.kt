package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendWelcomeMailUseCase
import io.miragon.example.application.port.out.NewsletterSubscriptionRepository
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SendWelcomeMailService(
    private val repository: NewsletterSubscriptionRepository,
    private val idempotencyGuard: IdempotentOperationExecutor
) : SendWelcomeMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendWelcomeMail(subscriptionId: SubscriptionId, operationId: OperationId) {
        idempotencyGuard.runOnce(operationId) {
            val subscription = repository.find(subscriptionId)
            log.info { "Sending welcome mail to ${subscription.email}" }
        }
    }
}
