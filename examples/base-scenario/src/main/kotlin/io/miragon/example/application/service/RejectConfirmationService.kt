package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.RejectConfirmationUseCase
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class RejectConfirmationService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess
) : RejectConfirmationUseCase {

    private val log = KotlinLogging.logger {}

    override fun reject(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        processPort.rejectConfirmation(membership.id)
        log.info { "Rejected confirmation of membership ${membership.id}" }
    }
}
