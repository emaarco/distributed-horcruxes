package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SendConfirmationMailService(
    private val repository: MembershipRepository,
    private val idempotencyGuard: IdempotentOperationExecutor
) : SendConfirmationMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendConfirmationMail(membershipId: MembershipId, operationId: OperationId) {
        idempotencyGuard.runOnce(operationId) {
            val membership = repository.find(membershipId)
            log.info { "Sending confirmation mail to ${membership.email.value}" }
        }
    }
}
