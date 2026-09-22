package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface ClaimMembershipUseCase {
    fun claim(membershipId: MembershipId): Boolean
}
