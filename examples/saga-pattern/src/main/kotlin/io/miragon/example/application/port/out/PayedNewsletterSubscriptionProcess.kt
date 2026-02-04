package io.miragon.example.application.port.out

import io.miragon.example.domain.SubscriptionId

interface PayedNewsletterSubscriptionProcess {
    fun submitForm(id: SubscriptionId)
}
