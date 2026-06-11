package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.out.db.message.MessageStatus
import io.miragon.example.adapter.out.db.message.ProcessMessageEntity
import io.miragon.example.adapter.out.db.message.ProcessMessageJpaRepository
import io.camunda.client.CamundaClient
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus

class ProcessEngineOutboxSchedulerTest {

    private val camundaClient = mockk<CamundaClient>(relaxed = true)
    private val repository = mockk<ProcessMessageJpaRepository>()
    private val underTest = ProcessEngineOutboxScheduler(camundaClient, txManager, repository)

    @Test
    fun `should do nothing when no pending messages exist`() {
        // Given
        every { repository.findFirstByStatusWithLock(MessageStatus.PENDING) } returns null

        // When
        underTest.sendMessages()

        // Then
        verify(exactly = 1) { repository.findFirstByStatusWithLock(MessageStatus.PENDING) }
        verify(exactly = 0) { camundaClient.newPublishMessageCommand() }
        confirmVerified(repository)
    }

    @Test
    fun `should send pending message to zeebe and mark it as sent`() {
        // Given
        val message = ProcessMessageEntity(
            messageName = "form-submitted",
            correlationId = "sub-123",
            variables = """{"subscriptionId":"sub-123"}"""
        )
        val sentMessage = message.copy(status = MessageStatus.SENT)

        every { repository.findFirstByStatusWithLock(MessageStatus.PENDING) } returnsMany listOf(message, null)
        every { repository.save(sentMessage) } returns sentMessage
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(any())
                .correlationKey(any())
                .messageId(any())
                .variables(any<Map<String, Any>>())
                .timeToLive(any())
                .send()
                .join()
        } returns mockk()

        // When
        underTest.sendMessages()

        // Then
        verify(exactly = 1) { repository.save(sentMessage) }
    }

    @Test
    fun `should increment retry count when zeebe send fails`() {
        // Given
        val message = ProcessMessageEntity(
            messageName = "form-submitted",
            correlationId = "sub-456",
            variables = """{"subscriptionId":"sub-456"}""",
            retryCount = 0
        )
        val retryMessage = message.copy(retryCount = 1)

        every { repository.findFirstByStatusWithLock(MessageStatus.PENDING) } returnsMany listOf(message, null)
        every { repository.save(retryMessage) } returns retryMessage
        every {
            camundaClient.newPublishMessageCommand()
                .messageName(any())
                .correlationKey(any())
                .messageId(any())
                .variables(any<Map<String, Any>>())
                .timeToLive(any())
                .send()
                .join()
        } throws RuntimeException("Zeebe unavailable")

        // When
        underTest.sendMessages()

        // Then
        verify(exactly = 1) { repository.save(retryMessage) }
    }

    companion object {
        private val txManager = object : AbstractPlatformTransactionManager() {
            override fun doGetTransaction() = Any()
            override fun doBegin(transaction: Any, definition: org.springframework.transaction.TransactionDefinition) {}
            override fun doCommit(status: DefaultTransactionStatus) {}
            override fun doRollback(status: DefaultTransactionStatus) {}
        }
    }
}
