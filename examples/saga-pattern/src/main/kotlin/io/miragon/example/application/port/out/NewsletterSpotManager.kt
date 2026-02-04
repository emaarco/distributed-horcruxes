package io.miragon.example.application.port.out

import io.miragon.example.domain.Email

interface NewsletterSpotManager {
    fun reserveSpot(email: Email): Boolean
    fun releaseSpot(email: Email)
    fun hasAvailableSpots(): Boolean
}
