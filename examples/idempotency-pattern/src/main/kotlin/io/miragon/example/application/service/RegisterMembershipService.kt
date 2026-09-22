package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.RegisterMembershipUseCase
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class RegisterMembershipService(
    private val repository: MembershipRepository,
    private val processPort: MembershipProcess
) : RegisterMembershipUseCase {

    private val log = KotlinLogging.logger {}

    override fun register(command: RegisterMembershipUseCase.Command): MembershipId {
        val membership = Membership(email = command.email, name = command.name)
        repository.save(membership)
        processPort.submitRegistration(membership.id)
        log.info { "Registered ${command.email} for the inner circle" }
        return membership.id
    }
}
