package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
import io.miragon.example.application.port.out.NewsletterSubscriptionProcess
import io.miragon.example.domain.SubscriptionId
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
        val messageId = "$correlationKey-$MESSAGE_FORM_SUBMITTED"

        camundaClient.newPublishMessageCommand()
            .messageName(MESSAGE_FORM_SUBMITTED)
            .correlationKey(correlationKey)
            .messageId(messageId)
            .variables(variables)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }

    override fun confirmSubscription(id: SubscriptionId) {
        val correlationKey = id.value.toString()
        val messageId = "$correlationKey-$MESSAGE_SUBSCRIPTION_CONFIRMED"

        camundaClient.newPublishMessageCommand()
            .messageName(MESSAGE_SUBSCRIPTION_CONFIRMED)
            .correlationKey(correlationKey)
            .messageId(messageId)
            .timeToLive(Duration.ofSeconds(10))
            .send()
            .join()
    }
}
