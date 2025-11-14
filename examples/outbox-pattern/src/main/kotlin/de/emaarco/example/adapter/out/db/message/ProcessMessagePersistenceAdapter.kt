package de.emaarco.example.adapter.out.db.message

import com.fasterxml.jackson.databind.ObjectMapper
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
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
        val processMessage = toProcessMessage(MESSAGE_FORM_SUBMITTED, id.value.toString(), variables)
        repository.save(processMessage)
    }

    override fun confirmSubscription(id: SubscriptionId) {
        val variables = mapOf("subscriptionId" to id.value.toString())
        val processMessage = toProcessMessage(MESSAGE_SUBSCRIPTION_CONFIRMED, id.value.toString(), variables)
        repository.save(processMessage)
        log.info { "Saved message for subscription-confirmation $id" }
    }

    private fun toProcessMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any>,
    ) = ProcessMessageEntity(
        messageName = messageName,
        correlationId = correlationId,
        variables = objectMapper.writeValueAsString(variables),
    )
}