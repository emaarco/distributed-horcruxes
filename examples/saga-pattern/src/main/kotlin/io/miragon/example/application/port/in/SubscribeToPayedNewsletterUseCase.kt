package io.miragon.example.application.port.`in`

import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import io.miragon.example.domain.SubscriptionId

interface SubscribeToPayedNewsletterUseCase {
    fun subscribe(command: Command): SubscriptionId

    data class Command(
        val email: Email,
        val name: Name
    )
}
