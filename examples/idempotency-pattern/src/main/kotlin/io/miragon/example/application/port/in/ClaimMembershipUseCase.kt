package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId

interface ClaimMembershipUseCase {
    fun claim(membershipId: MembershipId, operationId: OperationId): Boolean
}
