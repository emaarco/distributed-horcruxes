package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface ConfirmMembershipUseCase {
    fun confirm(membershipId: MembershipId)
}
