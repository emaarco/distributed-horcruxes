package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.SubscriptionId

interface SendConfirmationMailUseCase {
    fun sendConfirmationMail(subscriptionId: SubscriptionId)
}