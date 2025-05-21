package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_FormSubmitted
import de.emaarco.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.Message_SubscriptionConfirmed
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
            messageName = Message_FormSubmitted,
            correlationId = id.value.toString(),
            variables = variables
        )
    }

    override fun confirmSubscription(id: SubscriptionId) {
        engineApi.sendMessage(
            messageName = Message_SubscriptionConfirmed,
            correlationId = id.value.toString(),
        )
    }

}