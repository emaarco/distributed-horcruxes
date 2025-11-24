package de.emaarco.example.application.port.out

import de.emaarco.example.domain.SubscriptionCounter

interface SubscriptionCounterRepository {
    fun find(): SubscriptionCounter
    fun save(counter: SubscriptionCounter)
}
