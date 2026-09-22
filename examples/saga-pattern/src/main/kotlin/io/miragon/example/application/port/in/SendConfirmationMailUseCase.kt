package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface SendConfirmationMailUseCase {
    fun sendConfirmationMail(membershipId: MembershipId)
}
