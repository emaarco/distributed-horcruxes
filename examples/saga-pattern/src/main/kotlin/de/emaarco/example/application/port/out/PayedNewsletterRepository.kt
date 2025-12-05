package de.emaarco.example.application.port.out

import de.emaarco.example.domain.PayedNewsletterSubscription
import de.emaarco.example.domain.SubscriptionId

interface PayedNewsletterRepository {
    fun save(subscription: PayedNewsletterSubscription)
    fun find(id: SubscriptionId): PayedNewsletterSubscription
}
