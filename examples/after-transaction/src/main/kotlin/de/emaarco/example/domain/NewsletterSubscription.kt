package de.emaarco.example.domain

import java.time.LocalDateTime
import java.util.*

data class NewsletterSubscription(
    val id: SubscriptionId = SubscriptionId(UUID.randomUUID()),
    val name: Name,
    val email: Email,
    val newsletter: NewsletterId,
    val registrationDate: LocalDateTime = LocalDateTime.now(),
    val status: SubscriptionStatus = SubscriptionStatus.PENDING,
) {
    fun confirmRegistration() = this.copy(status = SubscriptionStatus.CONFIRMED)
    fun abortRegistration() = this.copy(status = SubscriptionStatus.ABORTED)
}
