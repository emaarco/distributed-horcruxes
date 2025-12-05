package de.emaarco.example.adapter.out.zeebe

import de.emaarco.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Messages
import de.emaarco.example.adapter.process.PayedNewsletterSubscriptionProcessApi.Variables
import de.emaarco.example.application.port.out.PayedNewsletterSubscriptionProcess
import de.emaarco.example.domain.SubscriptionId
import io.camunda.client.CamundaClient
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class PayedNewsletterProcessAdapter(
    private val camundaClient: CamundaClient
) : PayedNewsletterSubscriptionProcess {

    private val log = KotlinLogging.logger {}

    override fun submitForm(id: SubscriptionId) {
        val messageName = Messages.MESSAGE_FORM_SUBMITTED
        val variables = mapOf(Variables.SUBSCRIPTION_ID to id.value.toString())
        log.info { "Publishing message $messageName with variables $variables" }
        camundaClient.newPublishMessageCommand()
            .messageName(messageName)
            .withoutCorrelationKey()
            .variables(variables)
            .send()
            .join()
    }
}
