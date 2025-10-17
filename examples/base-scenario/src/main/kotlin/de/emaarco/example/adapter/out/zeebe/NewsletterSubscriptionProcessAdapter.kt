package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_FormSubmitted
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_SubscriptionConfirmed
import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import io.camunda.zeebe.client.ZeebeClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Base scenario: Direct process engine calls without transaction safety.
 * This adapter calls the process engine immediately, which can lead to:
 * 1. Process starting before database commit
 * 2. Process advancing even if database transaction fails
 * 3. Inconsistent state between database and process engine
 */
@Component
class NewsletterSubscriptionProcessAdapter(
    private val zeebeClient: ZeebeClient
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        // PROBLEM: This call happens immediately, potentially before the DB commit!
        val variables = mapOf("subscriptionId" to id.value.toString())
        val allVariables = variables + mapOf("correlationId" to id.value.toString())
        zeebeClient.newPublishMessageCommand()
            .messageName(Message_FormSubmitted)
            .withoutCorrelationKey()
            .variables(allVariables)
            .send()
            .join()
    }

    override fun confirmSubscription(id: SubscriptionId) {
        // PROBLEM: This call happens immediately, potentially before the DB commit!
        zeebeClient.newPublishMessageCommand()
            .messageName(Message_SubscriptionConfirmed)
            .correlationKey(id.value.toString())
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }

}
