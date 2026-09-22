package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.ServiceTasks
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Variables
import io.miragon.example.application.port.`in`.ClaimMembershipUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

/**
 * Demonstrates the idempotency problem: the spot is reserved, then the job
 * randomly fails before Zeebe learns about the completion. The retry reserves
 * a second spot for the same membership.
 */
@Component
class ClaimMembershipWorker(private val useCase: ClaimMembershipUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_CLAIM_MEMBERSHIP)
    fun claimMembership(@Variable("membershipId") membershipId: String): Map<String, Boolean> {
        log.debug { "Received Zeebe job to claim membership: $membershipId" }
        val hasEmptySpots = useCase.claim(MembershipId(UUID.fromString(membershipId)))
        if (Math.random() > 0.8) {
            throw RuntimeException("Simulating error on acknowledging, leading to idempotency problem")
        }
        return mapOf(Variables.ServiceTaskClaimMembership.HAS_EMPTY_SPOTS.value to hasEmptySpots)
    }
}
