package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface SendRejectionMailUseCase {
    fun sendRejectionMail(membershipId: MembershipId)
}
