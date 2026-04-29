package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_FORM_SUBMITTED
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Messages.MESSAGE_SUBSCRIPTION_CONFIRMED
import io.miragon.example.adapter.process.NewsletterSubscriptionProcessApi.Variables
import io.miragon.example.domain.SubscriptionId
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class NewsletterSubscriptionProcessAdapterTest {

    private val engineApi = mockk<ProcessEngineApi>()
    private val underTest = NewsletterSubscriptionProcessAdapter(engineApi)

    @Test
    fun `should start process via message when submitting form`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())
        val expectedVariables = mapOf(
            Variables.StartEventSubmitRegistrationForm.SUBSCRIPTION_ID.value to subscriptionId.value.toString()
        )

        every { engineApi.startProcessViaMessage(any(), any(), any()) } returns mockk()

        // When
        underTest.submitForm(subscriptionId)

        // Then
        verify(exactly = 1) {
            engineApi.startProcessViaMessage(
                messageName = MESSAGE_FORM_SUBMITTED.value,
                correlationId = subscriptionId.value.toString(),
                variables = expectedVariables
            )
        }
        confirmVerified(engineApi)
    }

    @Test
    fun `should send message when confirming subscription`() {
        // Given
        val subscriptionId = SubscriptionId(UUID.randomUUID())

        every { engineApi.sendMessage(any(), any(), any()) } returns mockk()

        // When
        underTest.confirmSubscription(subscriptionId)

        // Then
        verify(exactly = 1) {
            engineApi.sendMessage(
                messageName = MESSAGE_SUBSCRIPTION_CONFIRMED.value,
                correlationId = subscriptionId.value.toString()
            )
        }
        confirmVerified(engineApi)
    }
}
