package io.miragon.example.application.port.`in`

import io.miragon.example.domain.SubscriptionId

interface ProcessPaymentUseCase {
    fun processPayment(subscriptionId: SubscriptionId): Boolean
}
