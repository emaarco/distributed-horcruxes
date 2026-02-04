package io.miragon.example.adapter.out.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface NewsletterSubscriptionJpaRepository : JpaRepository<NewsletterSubscriptionEntity, UUID> {
    fun findBySubscriptionId(id: UUID): NewsletterSubscriptionEntity?
}