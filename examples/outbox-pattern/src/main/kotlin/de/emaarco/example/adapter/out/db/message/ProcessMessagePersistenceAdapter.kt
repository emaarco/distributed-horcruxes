package de.emaarco.example.adapter.out.db.message

import com.fasterxml.jackson.databind.ObjectMapper
import de.emaarco.example.adapter.out.zeebe.NewsletterSubscriptionProcessElements.MESSAGE_FORM_SUBMITTED
import de.emaarco.example.adapter.out.zeebe.NewsletterSubscriptionProcessElements.MESSAGE_RECEIVE_CONFIRMATION
import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ProcessMessagePersistenceAdapter(
    private val repository: ProcessMessageJpaRepository,
) : NewsletterSubscriptionProcess {

    private val log = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper()

    override fun submitForm(id: SubscriptionId) {
        val variables = mapOf("subscriptionId" to id.value.toString())
        val processMessage = ProcessMessageEntity(
            messageName = MESSAGE_FORM_SUBMITTED,
            correlationId = null,
            variables = objectMapper.writeValueAsString(variables),
        )
        repository.save(processMessage)
    }

    override fun confirmSubscription(id: SubscriptionId) {
        try {
            val variables = mapOf("subscriptionId" to id.value.toString())
            val processMessage = ProcessMessageEntity(
                messageName = MESSAGE_RECEIVE_CONFIRMATION,
                correlationId = null,
                variables = objectMapper.writeValueAsString(variables),
            )
            repository.save(processMessage)
            log.info { "Saved message for subscription-confirmation $id" }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}