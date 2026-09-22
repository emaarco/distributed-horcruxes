package io.miragon.example.adapter.`in`.zeebe

import io.miragon.example.application.port.`in`.ClaimMembershipUseCase
import io.miragon.example.domain.MembershipId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class ClaimMembershipWorkerTest {

    private val useCase = mockk<ClaimMembershipUseCase>()
    private val underTest = ClaimMembershipWorker(useCase)

    @Test
    fun `should claim membership and return capacity result`() {
        // Given
        val membershipIdString = "123e4567-e89b-12d3-a456-426614174000"
        val membershipId = MembershipId(UUID.fromString(membershipIdString))
        every { useCase.claim(membershipId) } returns true

        // When
        val result = runCatching { underTest.claimMembership(membershipIdString) }

        // Then
        verify(exactly = 1) { useCase.claim(membershipId) }
        result.onSuccess { variables -> assert(variables["hasEmptySpots"] == true) }
    }
}
