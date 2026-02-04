package io.miragon.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.response.PublishMessageResponse
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProcessEngineApiTest {

    private val camundaClient = mockk<CamundaClient>()
    private val processManager = ProcessEngineSynchronizer(camundaClient = camundaClient)
    private val underTest = ProcessEngineApi(
        camundaClient = camundaClient,
        manager = processManager
    )

    @Test
    fun `start process via message`() {
        val response = mockk<PublishMessageResponse>()
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(any())
                .withoutCorrelationKey()
                .variables(any<Map<String, Any>>())
                .send()
                .join()
        } returns response

        val actualFuture = underTest.startProcessViaMessage(
            messageName = "testMessage",
            correlationId = "12345",
            variables = mapOf("key" to "value")
        )

        val actualResponse = actualFuture.get()
        assertThat(actualResponse).isEqualTo(response)
    }

    @Test
    fun `send message to zeebe`() {

        val response = mockk<PublishMessageResponse>()
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(any())
                .correlationKey(any())
                .variables(any<Map<String, Any>>())
                .timeToLive(any())
                .send().join()
        } returns response

        val actualFuture = underTest.sendMessage(
            messageName = "testMessage",
            correlationId = "12345",
            variables = mapOf("key" to "value")
        )

        val actualResponse = actualFuture.get()
        assertThat(actualResponse).isEqualTo(response)
    }

}