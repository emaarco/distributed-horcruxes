package de.emaarco.example.adapter.out.memory

import de.emaarco.example.application.port.out.NewsletterSpotManager
import de.emaarco.example.domain.Email
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class InMemoryNewsletterSpotManager : NewsletterSpotManager {

    private val log = KotlinLogging.logger {}
    private val spots = mutableSetOf<Email>()

    companion object {
        const val TOTAL_SPOTS = 50
    }

    override fun reserveSpot(email: Email): Boolean {
        spots.add(email)
        log.info { "Reserved spot for ${email.value}. Total reservations: ${spots.size}" }
        return true
    }

    override fun releaseSpot(email: Email) {
        log.info { "Released spot for ${email.value}. Total reservations: ${spots.size}" }
        spots.remove(email)
    }

    override fun hasAvailableSpots(): Boolean {
        return spots.size < TOTAL_SPOTS
    }

}
