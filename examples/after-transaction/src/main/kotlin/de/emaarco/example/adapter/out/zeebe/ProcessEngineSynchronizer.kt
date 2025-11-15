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
 * Synchronizes process engine calls with database transactions to prevent inconsistent state.
 * Defers engine calls until after DB commit succeeds, and validates broker health before commit
 * to avoid orphaned database changes when the process engine is unavailable.
 */
@Component
class ProcessEngineSynchronizer(
    private val camundaClient: CamundaClient
) {

    private val log = KotlinLogging.logger {}

    /**
     * Executes engine call either immediately (no transaction) or deferred (inside transaction).
     * Deferral prevents race conditions where process tasks execute before DB changes are visible.
     */
    fun <T> executeEngineCall(
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
        val synchronization = ProcessEngineCallSynchronization(camundaClient, processEngineCall)
        TransactionSynchronizationManager.registerSynchronization(synchronization)
        return synchronization.future
    }

    /**
     * Spring transaction callback that validates broker health before commit and executes
     * the engine call after commit. Prevents DB changes from committing if broker is down.
     */
    private class ProcessEngineCallSynchronization<T>(
        private val camundaClient: CamundaClient,
        private val processEngineCall: () -> T
    ) : TransactionSynchronization {

        private val log = KotlinLogging.logger {}
        val future = CompletableFuture<T>()

        /**
         * Execute engine call after transaction commit.
         */
        override fun afterCommit() {
            try {
                val response = processEngineCall()
                future.complete(response)
            } catch (e: Exception) {
                log.error(e) { "Failed to execute process engine call" }
                future.completeExceptionally(e)
            }
        }

        /**
         * Validates the broker's health before commiting.
         * The broker is considered healthy if at least one partition is healthy.
         */
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