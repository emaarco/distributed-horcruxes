package de.emaarco.example.application.port.out

import de.emaarco.example.domain.SubscriptionId

interface PayedNewsletterSubscriptionProcess {
    fun submitForm(id: SubscriptionId)
}
