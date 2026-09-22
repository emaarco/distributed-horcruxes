package io.miragon.example.application.service

import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.application.port.out.ProcessedOperationRepository
import io.miragon.example.domain.*
import io.mockk.*
import org.junit.jupiter.api.Test
import java.util.*

class SendConfirmationMailServiceTest {

    private val repository = mockk<MembershipRepository>()
    private val processedOperationRepository = mockk<ProcessedOperationRepository>()
    private val underTest = SendConfirmationMailService(repository, IdempotentOperationExecutor(processedOperationRepository))

    private val membership = Membership(
        id = MembershipId(UUID.randomUUID()),
        name = Name("Test User"),
        email = Email("test@example.com"),
        status = MembershipStatus.CLAIMED
    )
    private val operationId = OperationId("${membership.id.value}-serviceTask_sendConfirmationMail")

    @Test
    fun `should load membership when operation is not processed yet`() {
        // Given
        every { processedOperationRepository.existsById(operationId) } returns false
        every { repository.find(membership.id) } returns membership
        every { processedOperationRepository.save(operationId) } just Runs

        // When
        underTest.sendConfirmationMail(membership.id, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 1) { repository.find(membership.id) }
        verify(exactly = 1) { processedOperationRepository.save(operationId) }
        confirmVerified(repository, processedOperationRepository)
    }

    @Test
    fun `should skip when operation is already processed`() {
        // Given
        every { processedOperationRepository.existsById(operationId) } returns true

        // When
        underTest.sendConfirmationMail(membership.id, operationId)

        // Then
        verify(exactly = 1) { processedOperationRepository.existsById(operationId) }
        verify(exactly = 0) { repository.find(any()) }
        verify(exactly = 0) { processedOperationRepository.save(any()) }
        confirmVerified(repository, processedOperationRepository)
    }
}
