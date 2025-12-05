package de.emaarco.example.adapter.out.db

import de.emaarco.example.application.port.out.PayedNewsletterRepository
import de.emaarco.example.domain.PayedNewsletterSubscription
import de.emaarco.example.domain.SubscriptionId
import org.springframework.stereotype.Component

@Component
class PayedNewsletterSubscriptionPersistenceAdapter(
    private val jpaRepository: PayedNewsletterSubscriptionJpaRepository,
    private val mapper: PayedNewsletterSubscriptionEntityMapper
) : PayedNewsletterRepository {

    override fun save(subscription: PayedNewsletterSubscription) {
        val entity = mapper.toEntity(subscription)
        jpaRepository.save(entity)
    }

    override fun find(id: SubscriptionId): PayedNewsletterSubscription {
        val entity = jpaRepository.findBySubscriptionId(id.value)
        return if (entity == null) {
            throw NoSuchElementException("Subscription not found: ${id.value}")
        } else {
            mapper.toDomain(entity)
        }
    }
}
