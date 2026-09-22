package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface RejectConfirmationUseCase {
    fun reject(membershipId: MembershipId)
}
