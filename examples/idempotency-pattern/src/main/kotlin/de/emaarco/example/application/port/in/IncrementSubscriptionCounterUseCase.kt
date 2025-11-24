package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId

interface IncrementSubscriptionCounterUseCase {
    fun incrementCounter(subscriptionId: SubscriptionId, operationId: OperationId)
}
