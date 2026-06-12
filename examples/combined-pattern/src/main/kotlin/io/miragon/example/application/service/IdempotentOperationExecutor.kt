package io.miragon.example.application.service

import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.domain.OperationId
import mu.KotlinLogging
import org.springframework.stereotype.Component

/**
 * Centralizes the idempotency check: skips the block if the operation
 * was already processed, otherwise executes it and records its completion.
 * Must be called inside the caller's transaction so that check,
 * business logic and record stay atomic.
 */
@Component
class IdempotentOperationExecutor(
    private val processedOperationRepository: ProcessedOperationRepository
) {

    private val log = KotlinLogging.logger {}

    fun runOnce(operationId: OperationId, block: () -> Unit) {
        if (processedOperationRepository.existsById(operationId)) {
            log.info { "Skipping already processed operation: ${operationId.value}" }
            return
        }

        block()
        processedOperationRepository.save(operationId)
    }
}
