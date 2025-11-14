package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
import de.emaarco.example.application.port.out.NewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
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