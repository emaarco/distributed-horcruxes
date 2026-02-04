package io.miragon.example.application.port.`in`

import io.miragon.example.domain.SubscriptionId

interface CancelReservationUseCase {
    fun cancelReservation(subscriptionId: SubscriptionId)
}
