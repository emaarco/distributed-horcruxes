package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.application.port.`in`.SendWelcomeMailUseCase
import io.miragon.example.domain.SubscriptionId
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Unit test for SendWelcomeMailWorker in base-scenario.
 * Tests that the worker correctly extracts variables and calls the use case.
 */
class SendWelcomeMailWorkerTest {

    private val useCase = mockk<SendWelcomeMailUseCase>()
    private val underTest = SendWelcomeMailWorker(useCase)

    @Test
    fun `should send welcome mail when job is received`() {
        // Given
        val subscriptionIdString = "123e4567-e89b-12d3-a456-426614174000"
        val subscriptionId = SubscriptionId(UUID.fromString(subscriptionIdString))

        every { useCase.sendWelcomeMail(subscriptionId) } just Runs

        // When
        underTest.sendConfirmationMail(subscriptionIdString)

        // Then
        verify(exactly = 1) { useCase.sendWelcomeMail(subscriptionId) }
    }
}
