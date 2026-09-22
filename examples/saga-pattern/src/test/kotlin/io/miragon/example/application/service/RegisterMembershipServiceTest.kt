package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.RegisterMembershipUseCase
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.Email
import io.miragon.example.domain.Membership
import io.miragon.example.domain.Name
import io.mockk.*
import org.junit.jupiter.api.Test

class RegisterMembershipServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val processPort = mockk<MembershipProcess>()
    private val underTest = RegisterMembershipService(repository, processPort)

    @Test
    fun `should create membership and notify process when registering`() {
        // Given
        val command = RegisterMembershipUseCase.Command(
            email = Email("test@example.com"),
            name = Name("Test User")
        )

        every { repository.save(any<Membership>()) } just Runs
        every { processPort.submitRegistration(any()) } just Runs

        // When
        val membershipId = underTest.register(command)

        // Then
        verify(exactly = 1) { repository.save(any()) }
        verify(exactly = 1) { processPort.submitRegistration(membershipId) }
        confirmVerified(repository, processPort)
    }
}
