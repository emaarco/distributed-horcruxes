package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.api.response.ActivatedJob
import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class SendConfirmationMailWorkerTest {

    private val useCase = mockk<SendConfirmationMailUseCase>()
    private val underTest = SendConfirmationMailWorker(useCase)

    @Test
    fun `should call use case with operation id when job is received`() {
        // Given
        val membershipIdString = "123e4567-e89b-12d3-a456-426614174000"
        val elementId = "serviceTask_sendConfirmationMail"
        val membershipId = MembershipId(UUID.fromString(membershipIdString))
        val operationId = OperationId("$membershipIdString-$elementId")

        val activatedJob = mockk<ActivatedJob>(relaxed = true)
        every { activatedJob.elementId } returns elementId
        every { useCase.sendConfirmationMail(membershipId, operationId) } just Runs

        // When
        underTest.sendConfirmationMail(activatedJob, membershipIdString)

        // Then
        verify(exactly = 1) { useCase.sendConfirmationMail(membershipId, operationId) }
    }
}
