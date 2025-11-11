package de.emaarco.example.application.port.out

import de.emaarco.example.domain.NewsletterSubscription
import de.emaarco.example.domain.SubscriptionId

interface NewsletterSubscriptionRepository {
    fun find(subscriptionId: SubscriptionId): NewsletterSubscription
    fun search(subscriptionId: SubscriptionId): NewsletterSubscription?
    fun save(subscription: NewsletterSubscription)
    fun delete(subscriptionId: SubscriptionId)
}
