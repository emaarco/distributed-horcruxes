package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.Email
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.Name
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class SendConfirmationMailServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val underTest = SendConfirmationMailService(repository)

    @Test
    fun `should load membership when handling sendConfirmationMail`() {
        // Given
        val membership = Membership(
            id = MembershipId(UUID.randomUUID()),
            name = Name("Test User"),
            email = Email("test@example.com")
        )
        every { repository.find(membership.id) } returns membership

        // When
        underTest.sendConfirmationMail(membership.id)

        // Then
        verify(exactly = 1) { repository.find(membership.id) }
        confirmVerified(repository)
    }
}
