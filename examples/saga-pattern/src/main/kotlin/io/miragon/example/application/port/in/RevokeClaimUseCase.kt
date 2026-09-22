package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface RevokeClaimUseCase {
    fun revoke(membershipId: MembershipId)
}
