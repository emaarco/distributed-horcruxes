package io.miragon.example.adapter.out.db.membership

import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.Membership
import io.miragon.example.domain.MembershipId
import org.springframework.stereotype.Component

@Component
class MembershipPersistenceAdapter(
    private val repository: MembershipJpaRepository
) : MembershipRepository {

    override fun find(membershipId: MembershipId): Membership {
        val entity = repository.findByMembershipId(membershipId.value) ?: throw NoSuchElementException()
        return MembershipEntityMapper.toDomain(entity)
    }

    override fun search(membershipId: MembershipId): Membership? {
        val entity = repository.findByMembershipId(membershipId.value) ?: return null
        return MembershipEntityMapper.toDomain(entity)
    }

    override fun save(membership: Membership) {
        val entity = MembershipEntityMapper.toEntity(membership)
        repository.save(entity)
    }

    override fun delete(membershipId: MembershipId) {
        repository.deleteById(membershipId.value)
    }
}
