package de.emaarco.example.adapter.out.zeebe

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.search.response.ProcessInstance
import io.camunda.zeebe.client.impl.search.filter.ProcessInstanceFilterImpl
import io.camunda.zeebe.client.protocol.rest.ProcessInstanceVariableFilterRequest
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit

@Component
class ProcessEngineApi(
    private val zeebeClient: ZeebeClient,
    private val manager: ProcessTransactionManager
) {

    fun startProcessViaMessage(
        messageName: String,
        correlationId: String,
        variables: Map<String, Any> = emptyMap(),
    ) = manager.executeAfterCommit {
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
    ) = manager.executeAfterCommit {
        zeebeClient.newPublishMessageCommand()
            .messageName(messageName)
            .correlationKey(correlationId)
            .variables(variables)
            .timeToLive(Duration.of(10, ChronoUnit.SECONDS))
            .send()
            .join()
    }

    fun searchProcessInstance(
        processId: String,
        correlationId: String,
    ): ProcessInstance? {
        val variableFilter = ProcessInstanceVariableFilterRequest().name("correlationId").values(listOf(correlationId))
        val filter = ProcessInstanceFilterImpl().bpmnProcessId(processId).variable(variableFilter)
        val queryResponse = zeebeClient.newProcessInstanceQuery().filter(filter).send().join()
        val instances = queryResponse.items()
        if (instances.size > 1) {
            throw RuntimeException("Expected max one process instance for processId=$processId and correlationId=$correlationId. Got ${instances.size}")
        } else {
            return instances.getOrNull(0)
        }
    }
}
