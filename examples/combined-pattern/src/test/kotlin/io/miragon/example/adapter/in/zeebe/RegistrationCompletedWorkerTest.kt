package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.application.port.`in`.IncrementSubscriptionCounterUseCase
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.api.response.ActivatedJob
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class RegistrationCompletedWorkerTest {

    private val useCase = mockk<IncrementSubscriptionCounterUseCase>()
    private val underTest = RegistrationCompletedWorker(useCase)

    @Test
    fun `should increment counter with operation id when registration completed job is received`() {
        // Given
        val subscriptionIdString = "123e4567-e89b-12d3-a456-426614174000"
        val elementId = "Activity_RegistrationCompleted"
        val subscriptionId = SubscriptionId(UUID.fromString(subscriptionIdString))
        val operationId = OperationId("$subscriptionIdString-$elementId")

        val activatedJob = mockk<ActivatedJob>(relaxed = true)
        every { activatedJob.elementId } returns elementId

        every { useCase.incrementCounter(subscriptionId, operationId) } just Runs

        // When
        underTest.handleRegistrationCompleted(activatedJob, subscriptionIdString)

        // Then
        verify(exactly = 1) { useCase.incrementCounter(subscriptionId, operationId) }
    }
}
