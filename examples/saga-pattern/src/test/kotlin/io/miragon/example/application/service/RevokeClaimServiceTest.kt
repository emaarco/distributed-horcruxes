package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipCapacity
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class RevokeClaimServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val capacity = mockk<MembershipCapacity>()
    private val underTest = RevokeClaimService(repository, capacity)

    @Test
    fun `should release spot and decline membership when claim is revoked`() {
        // Given
        val membership = Membership(
            id = MembershipId(UUID.randomUUID()),
            name = Name("Test User"),
            email = Email("test@example.com"),
            status = MembershipStatus.CLAIMED
        )
        every { repository.find(membership.id) } returns membership
        every { capacity.releaseSpot() } just Runs
        every { repository.save(any()) } just Runs

        // When
        underTest.revoke(membership.id)

        // Then
        verify(exactly = 1) { capacity.releaseSpot() }
        verify(exactly = 1) { repository.save(membership.copy(status = MembershipStatus.DECLINED)) }
    }
}
