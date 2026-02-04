package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.IncrementSubscriptionCounterUseCase
import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.application.port.out.SubscriptionCounterRepository
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class IncrementSubscriptionCounterService(
    private val counterRepository: SubscriptionCounterRepository,
    private val processedOperationRepository: ProcessedOperationRepository
) : IncrementSubscriptionCounterUseCase {

    private val log = KotlinLogging.logger {}

    override fun incrementCounter(subscriptionId: SubscriptionId, operationId: OperationId) {
        if (processedOperationRepository.existsById(operationId)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        val counter = counterRepository.find()
        val updatedCounter = counter.increment()
        counterRepository.save(updatedCounter)
        log.info { "Incremented subscription counter for ${subscriptionId.value}: ${updatedCounter.count}" }

        processedOperationRepository.save(operationId)
    }
}
