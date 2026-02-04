package io.miragon.example.adapter.out.memory

import io.miragon.example.application.port.out.SubscriptionCounterRepository
import io.miragon.example.domain.SubscriptionCounter
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class InMemorySubscriptionCounterRepository : SubscriptionCounterRepository {

    private val counter = AtomicInteger(0)

    override fun find(): SubscriptionCounter {
        return SubscriptionCounter(count = counter.get())
    }

    override fun save(counter: SubscriptionCounter) {
        this.counter.set(counter.count)
    }
}