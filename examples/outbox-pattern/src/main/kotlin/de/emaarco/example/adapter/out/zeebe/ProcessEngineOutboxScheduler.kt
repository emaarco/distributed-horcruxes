package de.emaarco.example.adapter.out.zeebe

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.emaarco.example.adapter.out.db.message.ProcessMessageEntity
import de.emaarco.example.adapter.out.db.message.ProcessMessageJpaRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ProcessEngineOutboxScheduler(
    private val engineApi: ProcessEngineApi,
    private val repository: ProcessMessageJpaRepository,
) {

    private val log = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    @Scheduled(fixedRate = 10000)
    fun sendMessages() {
        log.info { "Running scheduler to send messages to zeebe" }
        val messages = repository.findAll()
        messages.sortBy { it.createdAt }
        messages.forEach { message ->
            sendMessage(message)
            repository.delete(message)
            repository.flush()
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
}