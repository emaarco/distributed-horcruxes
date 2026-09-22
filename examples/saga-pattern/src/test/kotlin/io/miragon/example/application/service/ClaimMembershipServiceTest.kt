package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipCapacity
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class ClaimMembershipServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val capacity = mockk<MembershipCapacity>()
    private val underTest = ClaimMembershipService(repository, capacity)

    private val membership = Membership(
        id = MembershipId(UUID.randomUUID()),
        name = Name("Test User"),
        email = Email("test@example.com")
    )

    @Test
    fun `should claim membership when a spot is available`() {
        // Given
        every { repository.find(membership.id) } returns membership
        every { capacity.reserveSpot() } returns true
        every { repository.save(any()) } just Runs

        // When
        val hasEmptySpots = underTest.claim(membership.id)

        // Then
        assertTrue(hasEmptySpots)
        verify(exactly = 1) { repository.save(membership.copy(status = MembershipStatus.CLAIMED)) }
    }

    @Test
    fun `should reject membership when no spot is available`() {
        // Given
        every { repository.find(membership.id) } returns membership
        every { capacity.reserveSpot() } returns false
        every { repository.save(any()) } just Runs

        // When
        val hasEmptySpots = underTest.claim(membership.id)

        // Then
        assertFalse(hasEmptySpots)
        verify(exactly = 1) { repository.save(membership.copy(status = MembershipStatus.REJECTED)) }
    }
}
