package de.emaarco.example.adapter.out.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface PayedNewsletterSubscriptionJpaRepository : JpaRepository<PayedNewsletterSubscriptionEntity, UUID> {
    fun findBySubscriptionId(id: UUID): PayedNewsletterSubscriptionEntity?
}
