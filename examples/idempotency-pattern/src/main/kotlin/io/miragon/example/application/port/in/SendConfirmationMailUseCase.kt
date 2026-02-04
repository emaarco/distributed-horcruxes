package io.miragon.example.application.port.`in`

import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId

interface SendConfirmationMailUseCase {
    fun sendConfirmationMail(subscriptionId: SubscriptionId, operationId: OperationId)
}
