package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.ClaimMembershipUseCase
import io.miragon.example.application.port.out.MembershipCapacity
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class ClaimMembershipService(
    private val repository: MembershipRepository,
    private val capacity: MembershipCapacity
) : ClaimMembershipUseCase {

    private val log = KotlinLogging.logger {}

    override fun claim(membershipId: MembershipId): Boolean {
        val membership = repository.find(membershipId)
        val hasEmptySpots = capacity.reserveSpot()
        val claimedMembership = if (hasEmptySpots) membership.claim() else membership.reject()
        repository.save(claimedMembership)
        log.info { "Claimed membership ${membership.id} (has empty spots: $hasEmptySpots)" }
        return hasEmptySpots
    }
}
