package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.application.port.`in`.SendWelcomeMailUseCase
import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId
import io.camunda.client.api.response.ActivatedJob
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for SendWelcomeMailWorker in idempotency-pattern.
 * Tests that the worker correctly extracts variables, generates operation ID, and calls the use case.
 */
class SendWelcomeMailWorkerTest {

    private val useCase = mockk<SendWelcomeMailUseCase>()
    private val underTest = SendWelcomeMailWorker(useCase)

    @Test
    fun `should send welcome mail with operation id when job is received`() {
        // Given
        val subscriptionIdString = "123e4567-e89b-12d3-a456-426614174000"
        val elementId = "Activity_SendWelcomeMail"
        val subscriptionId = SubscriptionId(UUID.fromString(subscriptionIdString))
        val operationId = OperationId("$subscriptionIdString-$elementId")

        val activatedJob = mockk<ActivatedJob>(relaxed = true)
        every { activatedJob.elementId } returns elementId

        every { useCase.sendWelcomeMail(subscriptionId, operationId) } just Runs

        // When
        underTest.sendWelcomeMail(activatedJob, subscriptionIdString)

        // Then
        verify(exactly = 1) { useCase.sendWelcomeMail(subscriptionId, operationId) }
    }
}
