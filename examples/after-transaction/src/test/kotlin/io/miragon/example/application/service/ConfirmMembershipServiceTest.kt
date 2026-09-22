package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class ConfirmMembershipServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val processPort = mockk<MembershipProcess>()
    private val underTest = ConfirmMembershipService(repository, processPort)

    @Test
    fun `should confirm membership and notify process`() {
        // Given
        val membership = Membership(
            id = MembershipId(UUID.randomUUID()),
            name = Name("Test User"),
            email = Email("test@example.com"),
            status = MembershipStatus.CLAIMED
        )
        every { repository.find(membership.id) } returns membership
        every { repository.save(any()) } just Runs
        every { processPort.confirmMembership(membership.id) } just Runs

        // When
        underTest.confirm(membership.id)

        // Then
        verify(exactly = 1) { repository.save(membership.copy(status = MembershipStatus.CONFIRMED)) }
        verify(exactly = 1) { processPort.confirmMembership(membership.id) }
    }
}
