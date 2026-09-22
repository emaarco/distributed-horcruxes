package io.miragon.example.application.port.out

interface MembershipCapacity {
    fun reserveSpot(): Boolean
    fun releaseSpot()
}
