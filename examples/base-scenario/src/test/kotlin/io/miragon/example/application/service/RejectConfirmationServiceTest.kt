package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.Email
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.Name
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class RejectConfirmationServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val processPort = mockk<MembershipProcess>()
    private val underTest = RejectConfirmationService(repository, processPort)

    @Test
    fun `should notify process when confirmation is rejected`() {
        // Given
        val membership = Membership(
            id = MembershipId(UUID.randomUUID()),
            name = Name("Test User"),
            email = Email("test@example.com")
        )
        every { repository.find(membership.id) } returns membership
        every { processPort.rejectConfirmation(membership.id) } just Runs

        // When
        underTest.reject(membership.id)

        // Then
        verify(exactly = 1) { processPort.rejectConfirmation(membership.id) }
        confirmVerified(processPort)
    }
}
