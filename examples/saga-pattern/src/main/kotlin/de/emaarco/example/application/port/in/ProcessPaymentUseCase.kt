package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.SubscriptionId

interface ProcessPaymentUseCase {
    fun processPayment(subscriptionId: SubscriptionId): Boolean
}
