package io.miragon.example.adapter.`in`.zeebe

import io.camunda.client.annotation.JobWorker
import io.camunda.client.annotation.Variable
import io.miragon.example.adapter.process.InnerCircleMembershipCompensationProcessApi.ServiceTasks
import io.miragon.example.application.port.`in`.RevokeClaimUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class RevokeClaimWorker(private val useCase: RevokeClaimUseCase) {

    private val log = KotlinLogging.logger {}

    @JobWorker(type = ServiceTasks.MEMBERSHIP_REVOKE_CLAIM)
    fun revokeClaim(@Variable("membershipId") membershipId: String) {
        log.debug { "Received Zeebe compensation job to revoke claim: $membershipId" }
        useCase.revoke(MembershipId(UUID.fromString(membershipId)))
    }
}
