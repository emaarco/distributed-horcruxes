package de.emaarco.example.adapter.out.db

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "payed_newsletter_subscriptions")
data class PayedNewsletterSubscriptionEntity(
    @Id
    @Column(name = "subscription_id", nullable = false)
    val subscriptionId: UUID,

    @Column(name = "email", nullable = false)
    val email: String,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "payment_successful", nullable = true)
    val paymentSuccessful: Boolean? = null
)
