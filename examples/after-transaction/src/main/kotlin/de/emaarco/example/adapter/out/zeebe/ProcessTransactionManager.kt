package de.emaarco.example.adapter.out.zeebe

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.BrokerInfo
import io.camunda.zeebe.client.api.response.PartitionBrokerHealth
import io.camunda.zeebe.client.api.response.Topology
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Responsible for ensuring that engine calls are only executed once the current transaction has been completed.
 * This prevents subsequent tasks from being executed before the original transaction has been completed.
 * It also ensures that no calls are made to the process engine if the transaction fails.
 */
@Component
class ProcessTransactionManager(private val zeebeClient: ZeebeClient) {

    private val log = KotlinLogging.logger {}

    fun executeAfterCommit(processEngineCall: () -> Unit) {
        val isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive()
        if (isTransactionActive) {
            log.debug { "Registering process engine call after commit" }
            registerEngineCallAfterCommit(processEngineCall)
        } else {
            processEngineCall()
        }
    }

    private fun registerEngineCallAfterCommit(
        processEngineCall: () -> Unit
    ) = TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {

        override fun afterCommit() = try {
            processEngineCall()
        } catch (e: Exception) {
            log.error(e) { "Manual action required. Failed to execute process engine call after commit" }
            throw e
        }

        override fun beforeCommit(readOnly: Boolean) {
            val topology = zeebeClient.newTopologyRequest().send().join()
            val healthy = checkBrokerHealth(topology)
            if (!healthy) throw IllegalStateException("No healthy broker found")
        }

        private fun checkBrokerHealth(topology: Topology): Boolean {
            return topology.brokers.any { brokerIsHealthy(it) }
        }

        private fun brokerIsHealthy(broker: BrokerInfo): Boolean {
            return broker.partitions.any { it.health == PartitionBrokerHealth.HEALTHY }
        }
    })

}
