package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId

interface SendWelcomeMailUseCase {
    fun sendWelcomeMail(membershipId: MembershipId)
}
