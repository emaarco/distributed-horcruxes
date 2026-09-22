package io.miragon.example.adapter.out.db

import io.miragon.example.domain.*

object MembershipEntityMapper {

    fun toDomain(entity: MembershipEntity): Membership {
        return Membership(
            id = MembershipId(entity.membershipId),
            name = Name(entity.name),
            email = Email(entity.email),
            registrationDate = entity.registrationDate,
            status = entity.status
        )
    }

    fun toEntity(domain: Membership): MembershipEntity {
        return MembershipEntity(
            membershipId = domain.id.value,
            name = domain.name.value,
            email = domain.email.value,
            registrationDate = domain.registrationDate,
            status = domain.status
        )
    }

}
