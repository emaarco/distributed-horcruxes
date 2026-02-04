package io.miragon.example.application.port.`in`

import io.miragon.example.domain.SubscriptionId

interface AbortSubscriptionUseCase {
    fun abort(subscriptionId: SubscriptionId)
}