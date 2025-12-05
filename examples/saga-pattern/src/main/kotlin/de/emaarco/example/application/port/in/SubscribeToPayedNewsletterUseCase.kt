package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.Email
import de.emaarco.example.domain.Name
import de.emaarco.example.domain.SubscriptionId

interface SubscribeToPayedNewsletterUseCase {
    fun subscribe(command: Command): SubscriptionId

    data class Command(
        val email: Email,
        val name: Name
    )
}
