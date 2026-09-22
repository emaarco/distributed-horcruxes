package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.api.response.ActivatedJob
import io.miragon.example.application.port.`in`.ClaimMembershipUseCase
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class ClaimMembershipWorkerTest {

    private val useCase = mockk<ClaimMembershipUseCase>()
    private val underTest = ClaimMembershipWorker(useCase)

    @Test
    fun `should claim membership with operation id and return capacity result`() {
        // Given
        val membershipIdString = "123e4567-e89b-12d3-a456-426614174000"
        val elementId = "serviceTask_claimMembership"
        val membershipId = MembershipId(UUID.fromString(membershipIdString))
        val operationId = OperationId("$membershipIdString-$elementId")

        val activatedJob = mockk<ActivatedJob>(relaxed = true)
        every { activatedJob.elementId } returns elementId
        every { useCase.claim(membershipId, operationId) } returns true

        // When
        val variables = underTest.claimMembership(activatedJob, membershipIdString)

        // Then
        verify(exactly = 1) { useCase.claim(membershipId, operationId) }
        assertEquals(true, variables["hasEmptySpots"])
    }
}
