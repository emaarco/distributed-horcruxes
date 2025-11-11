package de.emaarco.example.application.port.`in`

import de.emaarco.example.domain.Email
import de.emaarco.example.domain.Name
import de.emaarco.example.domain.NewsletterId
import de.emaarco.example.domain.SubscriptionId

interface SubscribeToNewsletterUseCase {

    fun subscribe(command: Command): SubscriptionId

    data class Command(
        val email: Email,
        val name: Name,
        val newsletterId: NewsletterId
    )
}
