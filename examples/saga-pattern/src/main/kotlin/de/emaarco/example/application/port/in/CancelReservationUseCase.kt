package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.SubscriptionId

interface CancelReservationUseCase {
    fun cancelReservation(subscriptionId: SubscriptionId)
}
