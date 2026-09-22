package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipCapacity
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class ClaimMembershipServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val capacity = mockk<MembershipCapacity>()
    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = ClaimMembershipService(
        repository,
        capacity,
        IdempotentOperationExecutor(processedOperationRepository)
    )

    private val membership = Membership(
        id = MembershipId(UUID.randomUUID()),
        name = Name("Test User"),
        email = Email("test@example.com")
    )
    private val operationId = OperationId("${membership.id.value}-serviceTask_claimMembership")

    @Test
    fun `should reserve spot and claim membership when not processed yet`() {
        // Given
        every { processedOperationRepository.existsById(operationId) } returns false
        every { repository.find(membership.id) } returns membership andThen membership.claim()
        every { capacity.reserveSpot() } returns true
        every { repository.save(any()) } just Runs
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        val hasEmptySpots = underTest.claim(membership.id, operationId)

        // Then
        assertTrue(hasEmptySpots)
        verify(exactly = 1) { capacity.reserveSpot() }
        verify(exactly = 1) { repository.save(membership.claim()) }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
    }

    @Test
    fun `should reject membership when no spot is available`() {
        // Given
        every { processedOperationRepository.existsById(operationId) } returns false
        every { repository.find(membership.id) } returns membership andThen membership.reject()
        every { capacity.reserveSpot() } returns false
        every { repository.save(any()) } just Runs
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        val hasEmptySpots = underTest.claim(membership.id, operationId)

        // Then
        assertFalse(hasEmptySpots)
        verify(exactly = 1) { repository.save(membership.reject()) }
    }

    @Test
    fun `should not reserve another spot but return same answer when operation is already processed`() {
        // Given
        every { processedOperationRepository.existsById(operationId) } returns true
        every { repository.find(membership.id) } returns membership.claim()

        // When
        val hasEmptySpots = underTest.claim(membership.id, operationId)

        // Then
        assertTrue(hasEmptySpots)
        verify(exactly = 0) { capacity.reserveSpot() }
        verify(exactly = 0) { repository.save(any()) }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
    }
}
