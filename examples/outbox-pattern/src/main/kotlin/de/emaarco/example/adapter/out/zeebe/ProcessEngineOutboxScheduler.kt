package de.emaarco.example.adapter.out.zeebe

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.emaarco.example.adapter.out.db.message.MessageStatus
import de.emaarco.example.adapter.out.db.message.ProcessMessageEntity
import de.emaarco.example.adapter.out.db.message.ProcessMessageJpaRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * This scheduler runs every 200 ms to process and send outbox messages to Zeebe.
 * The fixed delay of 200 ms ensures that each execution completes before the next one starts,
 * preventing concurrent processing of the same messages.
 *
 * Uses SELECT FOR UPDATE SKIP LOCKED to allow multiple schedulers to run concurrently
 * without processing the same messages. Each scheduler locks one message at a time,
 * processes it, and commits the transaction before fetching the next message.
 */
@Component
class ProcessEngineOutboxScheduler(
    private val engineApi: ProcessEngineApi,
    private val transactionManager: PlatformTransactionManager,
    private val repository: ProcessMessageJpaRepository,
) {

    private val log = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()
    private val maxRetryCount = 3

    @Scheduled(fixedDelay = 200)
    fun sendMessages() {
        log.debug { "Running scheduler to send messages to zeebe" }
        var processedCount = 0

        // Process messages one at a time until none are left
        while (true) {
            val processed = processNextMessage()
            if (!processed) break
            processedCount++
        }

        if (processedCount > 0) {
            log.debug { "Processed $processedCount messages" }
        }
    }

    /**
     * Processes a single message within a transaction using pessimistic locking.
     * Returns true if a message was processed, false if no messages are available.
     */
    private fun processNextMessage(): Boolean {
        return performInTransaction {
            // Lock and fetch one message (SELECT FOR UPDATE SKIP LOCKED)
            val message = repository.findFirstByStatusWithLock(MessageStatus.PENDING)
                ?: return@performInTransaction false

            trySendMessage(message)
            true
        }
    }

    private fun trySendMessage(
        message: ProcessMessageEntity
    ) {
        try {
            sendMessage(message)
            val sentMessage = message.copy(status = MessageStatus.SENT)
            repository.save(sentMessage)
            log.info { "Successfully sent message ${message.messageName} with correlationId ${message.correlationId}" }
        } catch (e: Exception) {
            val retryCount = message.retryCount + 1
            if (retryCount >= maxRetryCount) {
                val failedMessage = message.copy(status = MessageStatus.FAILED)
                repository.save(failedMessage)
                log.error(e) { "Failed to send message ${message.messageName} after $maxRetryCount attempts" }
            } else {
                val retryMessage = message.copy(retryCount = retryCount)
                repository.save(retryMessage)
                log.warn(e) { "Retrying to send message ${message.messageName} (attempt $retryCount)" }
            }
        }
    }

    private fun sendMessage(message: ProcessMessageEntity) {
        val variables = objectMapper.readValue(message.variables, object : TypeReference<Map<String, Any>>() {})
        log.info { "Sending message ${message.messageName} with variables $variables" }
        engineApi.sendMessage(
            messageName = message.messageName,
            correlationId = message.correlationId,
            variables = variables,
        )
    }

    private fun <T> performInTransaction(block: () -> T): T {
        val template = TransactionTemplate(transactionManager)
        return template.execute { block() } ?: throw IllegalStateException("Transaction did not return a result")
    }
}
