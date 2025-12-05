package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.SubscriptionId

interface ReserveSpotUseCase {
    fun reserveSpot(subscriptionId: SubscriptionId)
}
