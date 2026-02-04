package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
import io.miragon.example.application.port.out.NewsletterSubscriptionProcess
import io.miragon.example.domain.SubscriptionId
import org.springframework.stereotype.Component

@Component
class NewsletterSubscriptionProcessAdapter(
    private val engineApi: ProcessEngineApi
) : NewsletterSubscriptionProcess {

    override fun submitForm(id: SubscriptionId) {
        val variables = mapOf("subscriptionId" to id.value.toString())
        engineApi.startProcessViaMessage(
            messageName = MESSAGE_FORM_SUBMITTED,
            correlationId = id.value.toString(),
            variables = variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        engineApi.sendMessage(
            messageName = MESSAGE_SUBSCRIPTION_CONFIRMED,
            correlationId = id.value.toString(),
        )
    }

}