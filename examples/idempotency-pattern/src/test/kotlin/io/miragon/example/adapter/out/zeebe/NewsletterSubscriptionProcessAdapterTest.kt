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
    fun `should publish form submitted message with message id when submitting form`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val correlationKey = subscriptionId.value.toString()
        val expectedMessageId = "$correlationKey-$MESSAGE_FORM_SUBMITTED"
        val response = mockk<PublishMessageResponse>()

        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_FORM_SUBMITTED.value)
                .correlationKey(correlationKey)
                .messageId(expectedMessageId)
                .variables(any<Map<String, Any>>())
                .timeToLive(any())
                .send()
                .join()
        } returns response

        // When
        underTest.submitForm(subscriptionId)

        // Then
        verify(exactly = 1) {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_FORM_SUBMITTED.value)
                .correlationKey(correlationKey)
                .messageId(expectedMessageId)
                .variables(any<Map<String, Any>>())
                .timeToLive(any())
                .send()
                .join()
        }
    }

    @Test
    fun `should publish subscription confirmed message with message id when confirming subscription`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val correlationKey = subscriptionId.value.toString()
        val expectedMessageId = "$correlationKey-$MESSAGE_SUBSCRIPTION_CONFIRMED"
        val response = mockk<PublishMessageResponse>()

        every {
            camundaClient.newPublishMessageCommand()
                .messageName(MESSAGE_SUBSCRIPTION_CONFIRMED.value)
                .correlationKey(correlationKey)
                .messageId(expectedMessageId)
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
                .correlationKey(correlationKey)
                .messageId(expectedMessageId)
                .timeToLive(any())
                .send()
                .join()
        }
    }
}
