package io.miragon.example.application.port.out

import io.miragon.example.domain.MembershipId

interface MembershipProcess {
    fun submitRegistration(id: MembershipId)
    fun confirmMembership(id: MembershipId)
    fun rejectConfirmation(id: MembershipId)
}
