package io.miragon.example.adapter.out.db.subscription

import io.miragon.example.domain.*

object NewsletterSubscriptionEntityMapper {

    fun toDomain(entity: NewsletterSubscriptionEntity): NewsletterSubscription {
        return NewsletterSubscription(
            id = SubscriptionId(entity.subscriptionId),
            name = Name(entity.name),
            email = Email(entity.email),
            newsletter = NewsletterId(entity.newsletterId),
            registrationDate = entity.registrationDate,
            status = entity.status
        )
    }

    fun toEntity(domain: NewsletterSubscription): NewsletterSubscriptionEntity {
        return NewsletterSubscriptionEntity(
            subscriptionId = domain.id.value,
            name = domain.name.value,
            email = domain.email.value,
            newsletterId = domain.newsletter.value,
            registrationDate = domain.registrationDate,
            status = domain.status
        )
    }

}