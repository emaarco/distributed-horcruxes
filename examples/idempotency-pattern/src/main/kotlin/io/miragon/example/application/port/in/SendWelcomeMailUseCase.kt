package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId

interface SendWelcomeMailUseCase {
    fun sendWelcomeMail(membershipId: MembershipId, operationId: OperationId)
}
