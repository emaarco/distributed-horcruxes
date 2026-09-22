package io.miragon.example.application.port.out

import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId

interface MembershipRepository {
    fun find(membershipId: MembershipId): Membership
    fun search(membershipId: MembershipId): Membership?
    fun save(membership: Membership)
    fun delete(membershipId: MembershipId)
}
