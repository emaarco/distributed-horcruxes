package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId

interface SendConfirmationMailUseCase {
    fun sendConfirmationMail(membershipId: MembershipId, operationId: OperationId)
}
