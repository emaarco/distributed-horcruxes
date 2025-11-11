package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_FormSubmitted
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_SubscriptionConfirmed
import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.CamundaClient
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class NewsletterSubscriptionProcessAdapter(
    private val camundaClient: CamundaClient
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        val correlationKey = id.value.toString()
        val variables = mapOf("subscriptionId" to correlationKey)
        val messageId = "$correlationKey-$Message_FormSubmitted"

        camundaClient.newPublishMessageCommand()
            .messageName(Message_FormSubmitted)
            .correlationKey(correlationKey)
            .messageId(messageId)
            .variables(variables)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }

    override fun confirmSubscription(id: SubscriptionId) {
        val correlationKey = id.value.toString()
        val messageId = "$correlationKey-$Message_SubscriptionConfirmed"

        camundaClient.newPublishMessageCommand()
            .messageName(Message_SubscriptionConfirmed)
            .correlationKey(correlationKey)
            .messageId(messageId)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }
}
