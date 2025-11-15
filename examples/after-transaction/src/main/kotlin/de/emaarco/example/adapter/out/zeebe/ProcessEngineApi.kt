package de.emaarco.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class ProcessEngineApi(
    private val camundaClient: CamundaClient,
    private val manager: ProcessEngineSynchronizer
) {

    fun startProcessViaMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any> = emptyMap(),
    ) = manager.executeEngineCall {
        val allVariables = variables + mapOf("correlationId" to correlationId)
        camundaClient.newPublishMessageCommand()
            .messageName(messageName)
            .withoutCorrelationKey()
            .variables(allVariables)
            .send()
            .join()
    }

    fun sendMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any> = emptyMap(),
    ) = manager.executeEngineCall {
        camundaClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationId)
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }
}
