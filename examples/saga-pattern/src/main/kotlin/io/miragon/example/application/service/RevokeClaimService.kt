package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.RevokeClaimUseCase
import io.miragon.example.application.port.out.MembershipCapacity
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class RevokeClaimService(
    private val repository: MembershipRepository,
    private val capacity: MembershipCapacity
) : RevokeClaimUseCase {

    private val log = KotlinLogging.logger {}

    override fun revoke(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        capacity.releaseSpot()
        repository.save(membership.decline())
        log.info { "Revoked claim of membership ${membership.id} and released its spot" }
    }
}
