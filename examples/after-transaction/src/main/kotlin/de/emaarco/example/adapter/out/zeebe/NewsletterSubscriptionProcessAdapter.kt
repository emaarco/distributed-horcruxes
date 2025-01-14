package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.out.zeebe.NewsletterSubscriptionProcessElements.MESSAGE_FORM_SUBMITTED
import de.emaarco.example.adapter.out.zeebe.NewsletterSubscriptionProcessElements.MESSAGE_RECEIVE_CONFIRMATION
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
            messageName = MESSAGE_RECEIVE_CONFIRMATION,
            correlationId = id.value.toString(),
        )
    }

}