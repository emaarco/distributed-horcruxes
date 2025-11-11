package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.OperationId
import de.emaarco.example.domain.SubscriptionId

interface SendWelcomeMailUseCase {
    fun sendWelcomeMail(subscriptionId: SubscriptionId, operationId: OperationId)
}
