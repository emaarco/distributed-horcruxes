package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_FormSubmitted
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_SubscriptionConfirmed
import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import org.springframework.stereotype.Component

/**
 * Base scenario: Direct process engine calls without transaction safety.
 * This adapter calls the process engine immediately, which can lead to:
 * 1. Process starting before database commit
 * 2. Process advancing even if database transaction fails
 * 3. Inconsistent state between database and process engine
 */
@Component
class NewsletterSubscriptionProcessAdapter(
    private val engineApi: ProcessEngineApi
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        val variables = mapOf("subscriptionId" to id.value.toString())
        // PROBLEM: This call happens immediately, potentially before DB commit!
        engineApi.startProcessViaMessage(
            messageName = Message_FormSubmitted,
            correlationId = id.value.toString(),
            variables = variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        // PROBLEM: This call happens immediately, potentially before DB commit!
        engineApi.sendMessage(
            messageName = Message_SubscriptionConfirmed,
            correlationId = id.value.toString(),
        )
    }

}
