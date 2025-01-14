package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.SubscriptionId

interface ConfirmSubscriptionUseCase {
    fun confirm(subscriptionId: SubscriptionId)
}