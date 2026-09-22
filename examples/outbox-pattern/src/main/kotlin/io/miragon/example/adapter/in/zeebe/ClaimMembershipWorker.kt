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

@Component
class ClaimMembershipWorker(private val useCase: ClaimMembershipUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_CLAIM_MEMBERSHIP)
    fun claimMembership(@Variable("membershipId") membershipId: String): Map<String, Boolean> {
        log.debug { "Received Zeebe job to claim membership: $membershipId" }
        val hasEmptySpots = useCase.claim(MembershipId(UUID.fromString(membershipId)))
        return mapOf(Variables.ServiceTaskClaimMembership.HAS_EMPTY_SPOTS.value to hasEmptySpots)
    }
}
