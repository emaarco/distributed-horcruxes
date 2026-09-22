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

class SendRejectionMailServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val underTest = SendRejectionMailService(repository)

    @Test
    fun `should load membership when handling sendRejectionMail`() {
        // Given
        val membership = Membership(
            id = MembershipId(UUID.randomUUID()),
            name = Name("Test User"),
            email = Email("test@example.com")
        )
        every { repository.find(membership.id) } returns membership

        // When
        underTest.sendRejectionMail(membership.id)

        // Then
        verify(exactly = 1) { repository.find(membership.id) }
        confirmVerified(repository)
    }
}
