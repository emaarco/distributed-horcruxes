package io.miragon.example.adapter.out.memory

import io.miragon.example.application.port.out.MembershipCapacity
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class InMemoryMembershipCapacity : MembershipCapacity {

    private val log = KotlinLogging.logger {}
    private val availableSpots = AtomicInteger(TOTAL_SPOTS)

    override fun reserveSpot(): Boolean {
        val spotsBeforeReservation = availableSpots.getAndUpdate { spots -> if (spots > 0) spots - 1 else spots }
        log.info { "Inner circle spots remaining: ${availableSpots.get()}" }
        return spotsBeforeReservation > 0
    }

    companion object {
        const val TOTAL_SPOTS = 5
    }
}
