package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.ConfirmMembershipUseCase
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class ConfirmMembershipService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess
) : ConfirmMembershipUseCase {

    private val log = KotlinLogging.logger {}

    override fun confirm(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        repository.save(membership.confirm())
        processPort.confirmMembership(membership.id)
        log.info { "Confirmed membership ${membership.id}" }
    }
}
