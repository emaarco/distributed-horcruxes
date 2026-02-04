package io.miragon.example.application.port.`in`

import io.miragon.example.domain.SubscriptionId

interface SendWelcomeMailUseCase {
    fun sendWelcomeMail(subscriptionId: SubscriptionId)
}