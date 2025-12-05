package de.emaarco.example.adapter.out.db

import de.emaarco.example.domain.*
import org.springframework.stereotype.Component

@Component
class PayedNewsletterSubscriptionEntityMapper {

    fun toDomain(entity: PayedNewsletterSubscriptionEntity) = PayedNewsletterSubscription(
        id = SubscriptionId(entity.subscriptionId),
        email = Email(entity.email),
        name = Name(entity.name),
        paymentSuccessful = entity.paymentSuccessful
    )

    fun toEntity(domain: PayedNewsletterSubscription) = PayedNewsletterSubscriptionEntity(
        subscriptionId = domain.id.value,
        email = domain.email.value,
        name = domain.name.value,
        paymentSuccessful = domain.paymentSuccessful
    )
}
