package io.miragon.example.adapter.out.zeebe

import io.camunda.client.CamundaClient
import io.camunda.client.api.response.BrokerInfo
import io.camunda.client.api.response.PartitionBrokerHealth
import io.camunda.client.api.response.PartitionInfo
import io.camunda.client.api.response.Topology
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ExecutionException

/**
 * Integration tests using Spring's TransactionTemplate with real transaction lifecycle.
 */
class ProcessEngineSynchronizerTest {

    @Test
    fun `executes immediately when no transaction is active`() {

        // Given: No active transaction
        val underTest = ProcessEngineSynchronizer(mockk())

        // When: Execute engine call
        val result = underTest.executeEngineCall { "success-result" }

        // Then: Returns completed future immediately
        assertThat(result.get()).isEqualTo("success-result")
    }

    @Test
    fun `wraps exception in failed future when no transaction`() {

        // Given: No active transaction
        val underTest = ProcessEngineSynchronizer(mockk())

        // When: Engine call throws exception
        val result = underTest.executeEngineCall { throw RuntimeException("Engine failure") }

        // Then: Returns failed future
        assertThatThrownBy { result.get() }
            .isInstanceOf(ExecutionException::class.java)
            .hasMessageContaining("Failed to complete transaction synchronously")
    }

    @Test
    fun `defers execution until after transaction commit`() {

        // Given: Active transaction with a healthy broker
        val engineCall = mockk<() -> String>(relaxed = true)
        val synchronizer = createWithHealthyBroker()
        every { engineCall.invoke() } returns "async-result"

        // When: Execute within transaction
        val result = txTemplate.execute {
            synchronizer.executeEngineCall(engineCall)
        }

        // Then: Future completed with result
        if (result == null) fail("Result should not be null")
        assertThat(result.get()).isEqualTo("async-result")
    }

    @Test
    fun `rolls back transaction when broker is unhealthy`() {

        // Given: Unhealthy broker
        val engineCall = mockk<() -> String>(relaxed = true)
        val underTest = createWithUnhealthyBroker()

        // When: Execute within transaction
        // Then: Transaction rolls back, engine call never executed
        val result = assertThatThrownBy {
            txTemplate.execute { underTest.executeEngineCall(engineCall) }
        }

        result.isInstanceOf(IllegalStateException::class.java).hasMessageContaining("No healthy broker found")
        verify { engineCall wasNot Called }
    }

    @Test
    fun `completes future exceptionally when engine call fails after commit`() {

        // Given: Healthy broker with failing engine call
        val engineCall = mockk<() -> String>(relaxed = true)
        val underTest = createWithHealthyBroker()

        // When: Execute failing engine call within transaction
        val result = txTemplate.execute {
            underTest.executeEngineCall {
                engineCall()
                throw RuntimeException("Engine call failed")
            }
        }

        // Then: Future completed exceptionally, but the engine call was executed
        if (result == null) fail("Result should not be null")
        assertThatThrownBy { result.get() }.isInstanceOf(ExecutionException::class.java)
        verify { engineCall.invoke() }
    }

    private fun createWithHealthyBroker(): ProcessEngineSynchronizer {
        val client = mockk<CamundaClient>(relaxed = true)
        val partition = mockk<PartitionInfo> { every { health } returns PartitionBrokerHealth.HEALTHY }
        val broker = mockk<BrokerInfo> { every { partitions } returns listOf(partition) }
        val topology = mockk<Topology> { every { brokers } returns listOf(broker) }
        every { client.newTopologyRequest().send().join() } returns topology
        return ProcessEngineSynchronizer(client)
    }

    private fun createWithUnhealthyBroker(): ProcessEngineSynchronizer {
        val client = mockk<CamundaClient>(relaxed = true)
        val partition = mockk<PartitionInfo> { every { health } returns PartitionBrokerHealth.UNHEALTHY }
        val broker = mockk<BrokerInfo> { every { partitions } returns listOf(partition) }
        val topology = mockk<Topology> { every { brokers } returns listOf(broker) }
        every { client.newTopologyRequest().send().join() } returns topology
        return ProcessEngineSynchronizer(client)
    }


    private val txTemplate = TransactionTemplate(object : AbstractPlatformTransactionManager() {
        override fun doGetTransaction() = Any()
        override fun doBegin(transaction: Any, definition: org.springframework.transaction.TransactionDefinition) {}
        override fun doCommit(status: DefaultTransactionStatus) {}
        override fun doRollback(status: DefaultTransactionStatus) {}
    })
}
