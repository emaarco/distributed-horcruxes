package io.miragon.example.application.port.out

import io.miragon.example.domain.PayedNewsletterSubscription
import io.miragon.example.domain.SubscriptionId

interface PayedNewsletterRepository {
    fun save(subscription: PayedNewsletterSubscription)
    fun find(id: SubscriptionId): PayedNewsletterSubscription
}
