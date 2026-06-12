package io.miragon.example.application.service

import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.domain.OperationId
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for IdempotentOperationExecutor.
 * Tests the central Check → Execute → Record cycle.
 */
class IdempotentOperationExecutorTest {

    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = IdempotentOperationExecutor(processedOperationRepository)

    @Test
    fun `should execute block and record operation when not processed yet`() {
        // Given
        val operationId = OperationId("${UUID.randomUUID()}-Activity_SendWelcomeMail")
        val block = mockk<() -> Unit>()

        every { processedOperationRepository.existsById(operationId) } returns false
        every { block.invoke() } just Runs
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        underTest.runOnce(operationId, block)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 1) { block.invoke() }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
        confirmVerified(processedOperationRepository, block)
    }

    @Test
    fun `should skip block when operation is already processed`() {
        // Given
        val operationId = OperationId("${UUID.randomUUID()}-Activity_SendWelcomeMail")
        val block = mockk<() -> Unit>()

        every { processedOperationRepository.existsById(operationId) } returns true

        // When
        underTest.runOnce(operationId, block)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 0) { block.invoke() }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
        confirmVerified(processedOperationRepository, block)
    }
}
