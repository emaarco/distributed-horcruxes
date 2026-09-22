package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.ClaimMembershipUseCase
import io.miragon.example.application.port.out.MembershipCapacity
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.MembershipStatus
import io.miragon.example.domain.OperationId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class ClaimMembershipService(
    private val repository: MembershipRepository,
    private val capacity: MembershipCapacity,
    private val idempotencyGuard: IdempotentOperationExecutor
) : ClaimMembershipUseCase {

    private val log = KotlinLogging.logger {}

    override fun claim(membershipId: MembershipId, operationId: OperationId): Boolean {
        idempotencyGuard.runOnce(operationId) {
            val membership = repository.find(membershipId)
            val hasEmptySpots = capacity.reserveSpot()
            val claimedMembership = if (hasEmptySpots) membership.claim() else membership.reject()
            repository.save(claimedMembership)
            log.info { "Claimed membership ${membership.id} (has empty spots: $hasEmptySpots)" }
        }
        return repository.find(membershipId).status == MembershipStatus.CLAIMED
    }
}
