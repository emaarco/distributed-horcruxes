package io.miragon.example.domain

import java.time.LocalDateTime
import java.util.*

data class Membership(
    val id: MembershipId = MembershipId(UUID.randomUUID()),
    val name: Name,
    val email: Email,
    val registrationDate: LocalDateTime = LocalDateTime.now(),
    val status: MembershipStatus = MembershipStatus.PENDING,
) {
    fun claim() = this.copy(status = MembershipStatus.CLAIMED)
    fun reject() = this.copy(status = MembershipStatus.REJECTED)
    fun confirm() = this.copy(status = MembershipStatus.CONFIRMED)
}
