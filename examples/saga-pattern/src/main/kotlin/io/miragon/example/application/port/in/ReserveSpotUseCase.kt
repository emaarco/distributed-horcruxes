package io.miragon.example.application.port.`in`

import io.miragon.example.domain.SubscriptionId

interface ReserveSpotUseCase {
    fun reserveSpot(subscriptionId: SubscriptionId)
}
