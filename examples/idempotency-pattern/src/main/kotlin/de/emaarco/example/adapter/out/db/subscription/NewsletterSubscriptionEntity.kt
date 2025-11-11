package de.emaarco.example.adapter.out.db.subscription

import de.emaarco.example.domain.SubscriptionStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity(name = "newsletter_subscription")
data class NewsletterSubscriptionEntity(

    @Id
    @Column(name = "subscription_id", nullable = false)
    val subscriptionId: UUID,

    @Column(name = "subscriber_name", nullable = false)
    val name: String,

    @Column(name = "subscriber_mail", nullable = false)
    val email: String,

    @Column(name = "newsletter_id", nullable = false)
    val newsletterId: UUID,

    @Column(name = "registration_date", nullable = false)
    val registrationDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    val status: SubscriptionStatus = SubscriptionStatus.PENDING

)
