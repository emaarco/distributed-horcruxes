package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.CamundaClient
import io.camunda.client.api.response.PublishMessageResponse
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class NewsletterSubscriptionProcessAdapterTest {

    private val camundaClient = mockk<CamundaClient>(relaxed = true)
    private val underTest = NewsletterSubscriptionProcessAdapter(camundaClient)

    @Test
    fun `should publish form submitted message when submitting form`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val response = mockk<PublishMessageResponse>()

        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_FORM_SUBMITTED.value)
                .withoutCorrelationKey()
                .variables(any<Map<String, Any>>())
                .send()
                .join()
        } returns response

        // When
        underTest.submitForm(subscriptionId)

        // Then
        verify(exactly = 1) {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_FORM_SUBMITTED.value)
                .withoutCorrelationKey()
                .variables(any<Map<String, Any>>())
                .send()
                .join()
        }
    }

    @Test
    fun `should publish subscription confirmed message when confirming subscription`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val response = mockk<PublishMessageResponse>()

        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_SUBSCRIPTION_CONFIRMED.value)
                .correlationKey(subscriptionId.value.toString())
                .timeToLive(any())
                .send()
                .join()
        } returns response

        // When
        underTest.confirmSubscription(subscriptionId)

        // Then
        verify(exactly = 1) {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_SUBSCRIPTION_CONFIRMED.value)
                .correlationKey(subscriptionId.value.toString())
                .timeToLive(any())
                .send()
                .join()
        }
    }
}
