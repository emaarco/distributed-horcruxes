package io.miragon.example.adapter.out.db

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MembershipJpaRepository : JpaRepository<MembershipEntity, UUID> {
    fun findByMembershipId(id: UUID): MembershipEntity?
}
