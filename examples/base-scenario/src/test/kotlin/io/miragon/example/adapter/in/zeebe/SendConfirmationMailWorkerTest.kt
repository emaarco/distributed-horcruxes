package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for SendConfirmationMailWorker in base-scenario.
 * Tests that the worker correctly extracts variables and calls the use case.
 */
class SendConfirmationMailWorkerTest {

    private val useCase = mockk<SendConfirmationMailUseCase>()
    private val underTest = SendConfirmationMailWorker(useCase)

    @Test
    fun `should send confirmation mail when job is received`() {
        // Given
        val subscriptionIdString = "123e4567-e89b-12d3-a456-426614174000"
        val subscriptionId = SubscriptionId(UUID.fromString(subscriptionIdString))

        every { useCase.sendConfirmationMail(subscriptionId) } just Runs

        // When
        underTest.sendConfirmationMail(subscriptionIdString)

        // Then
        verify(exactly = 1) { useCase.sendConfirmationMail(subscriptionId) }
    }
}
