package io.miragon.example.application.port.`in`

import io.miragon.example.domain.SubscriptionId

interface ConfirmSubscriptionUseCase {
    fun confirm(subscriptionId: SubscriptionId)
}