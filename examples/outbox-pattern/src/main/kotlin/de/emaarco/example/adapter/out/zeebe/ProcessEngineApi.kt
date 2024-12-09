package de.emaarco.example.adapter.out.zeebe

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep2
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class ProcessEngineApi(
    private val zeebeClient: ZeebeClient,
    private val manager: ProcessTransactionManager
) {

    fun sendMessage(
        messageName: String,
        correlationId: String?,
        variables: Map<String, Any> = emptyMap(),
    ) = manager.executeAfterCommit {
        zeebeClient.newPublishMessageCommand()
            .messageName(messageName)
            .applyCorrelationKey(correlationId)
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }

    private fun PublishMessageCommandStep2.applyCorrelationKey(
        correlationId: String?
    ) = if (correlationId != null) {
        correlationKey(correlationId)
    } else {
        withoutCorrelationKey()
    }

}
