package de.emaarco.example.application.port.out

import de.emaarco.example.domain.Email

interface NewsletterSpotManager {
    fun reserveSpot(email: Email): Boolean
    fun releaseSpot(email: Email)
    fun hasAvailableSpots(): Boolean
}
