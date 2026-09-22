package io.miragon.example.application.port.`in`

import io.miragon.example.domain.MembershipId
import io.miragon.example.domain.OperationId

interface SendRejectionMailUseCase {
    fun sendRejectionMail(membershipId: MembershipId, operationId: OperationId)
}
