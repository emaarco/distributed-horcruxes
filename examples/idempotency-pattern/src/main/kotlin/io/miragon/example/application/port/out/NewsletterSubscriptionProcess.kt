package io.miragon.example.application.port.out

import io.miragon.example.domain.SubscriptionId

interface NewsletterSubscriptionProcess {
    fun submitForm(id: SubscriptionId)
    fun confirmSubscription(id: SubscriptionId)
}
