package de.emaarco.example.adapter.out.zeebe

import io.camunda.zeebe.client.ZeebeClient
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Base scenario: Direct Zeebe calls without any transaction safety.
 * This demonstrates the distributed transaction problem where the process engine
 * is notified BEFORE the database transaction commits, potentially leading to
 * inconsistent states.
 */
@Component
class ProcessEngineApi(
    private val zeebeClient: ZeebeClient
) {

    fun startProcessViaMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any> = emptyMap(),
    ) {
        // WARNING: This call happens immediately, potentially before DB commit!
        val allVariables = variables + mapOf("correlationId" to correlationId)
        zeebeClient.newPublishMessageCommand()
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
    ) {
        // WARNING: This call happens immediately, potentially before DB commit!
        zeebeClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationId)
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }
}
