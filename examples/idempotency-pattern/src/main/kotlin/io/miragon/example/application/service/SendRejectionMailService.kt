package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendRejectionMailUseCase
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class SendRejectionMailService(
    private val repository: MembershipRepository,
    private val idempotencyGuard: IdempotentOperationExecutor
) : SendRejectionMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendRejectionMail(membershipId: MembershipId, operationId: OperationId) {
        idempotencyGuard.runOnce(operationId) {
            val membership = repository.find(membershipId)
            log.info { "Sending rejection mail to ${membership.email.value}" }
        }
    }
}
