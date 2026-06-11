package io.miragon.example.application.port.out

import io.miragon.example.domain.SubscriptionCounter

interface SubscriptionCounterRepository {
    fun find(): SubscriptionCounter
    fun save(counter: SubscriptionCounter)
}
