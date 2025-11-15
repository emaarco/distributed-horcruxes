package de.emaarco.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.response.BrokerInfo
import io.camunda.client.api.response.PartitionBrokerHealth
import io.camunda.client.api.response.Topology
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * Responsible for ensuring that engine calls are only executed once the current transaction has been completed.
 * This prevents later tasks from being executed before the original transaction has been completed.
 * It also ensures that no calls are made to the process engine if the transaction fails.
 */
@Component
class ProcessTransactionManager(
    private val camundaClient: CamundaClient
) {

    private val log = KotlinLogging.logger {}

    fun <T> executeAfterCommit(
        processEngineCall: () -> T
    ): Future<T> {
        val isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive()
        return if (isTransactionActive) {
            log.debug { "Found transaction, executing process-engine-call after commit" }
            completeAfterCommit(processEngineCall)
        } else {
            log.debug { "No transaction active, executing process-engine-call synchronously" }
            completeSync(processEngineCall)
        }
    }

    private fun <V> completeSync(
        processEngineCall: () -> V
    ) = try {
        val response = processEngineCall()
        CompletableFuture.completedFuture(response)
    } catch (e: Exception) {
        val error = IllegalStateException("Failed to complete transaction synchronously", e)
        CompletableFuture.failedFuture(error)
    }

    private fun <V> completeAfterCommit(
        processEngineCall: () -> V
    ): CompletableFuture<V> {
        val synchronization = CustomSynchronization(camundaClient, processEngineCall)
        TransactionSynchronizationManager.registerSynchronization(synchronization)
        return synchronization.future
    }

    /**
     * Synchronization that executes the process engine call after the current transaction has been committed.
     * If the transaction fails, the future is completed exceptionally.
     */
    class CustomSynchronization<T>(
        private val camundaClient: CamundaClient,
        private val processEngineCall: () -> T
    ) : TransactionSynchronization {

        private val log = KotlinLogging.logger {}
        val future = CompletableFuture<T>()

        override fun afterCommit() {
            try {
                val response = processEngineCall()
                future.complete(response)
            } catch (e: Exception) {
                log.error(e) { "Failed to execute process engine call" }
                future.completeExceptionally(e)
            }
        }

        override fun beforeCommit(readOnly: Boolean) {
            val topology = camundaClient.newTopologyRequest().send().join()
            val healthy = checkBrokerHealth(topology)
            if (!healthy) {
                val exception = IllegalStateException("No healthy broker found")
                future.completeExceptionally(exception)
                throw exception
            }
        }

        private fun checkBrokerHealth(topology: Topology): Boolean {
            return topology.brokers.any { brokerIsHealthy(it) }
        }

        private fun brokerIsHealthy(broker: BrokerInfo): Boolean {
            return broker.partitions.any { it.health == PartitionBrokerHealth.HEALTHY }
        }
    }

}