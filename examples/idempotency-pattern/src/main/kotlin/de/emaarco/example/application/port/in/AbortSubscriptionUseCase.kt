package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId

interface AbortSubscriptionUseCase {
    fun abort(subscriptionId: SubscriptionId, operationId: OperationId)
}
