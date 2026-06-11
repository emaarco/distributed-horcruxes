package io.miragon.example.application.port.`in`

import io.miragon.example.domain.OperationId
import io.miragon.example.domain.SubscriptionId

interface IncrementSubscriptionCounterUseCase {
    fun incrementCounter(subscriptionId: SubscriptionId, operationId: OperationId)
}
