package io.miragon.example.adapter.`in`.rest

import io.miragon.example.application.port.`in`.ConfirmMembershipUseCase
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/memberships")
class ConfirmMembershipController(private val useCase: ConfirmMembershipUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/confirm/{membershipId}")
    fun confirmMembership(@PathVariable membershipId: String): ResponseEntity<Void> {
        log.debug { "Received REST-request to confirm membership: $membershipId" }
        useCase.confirm(MembershipId(UUID.fromString(membershipId)))
        return ResponseEntity.ok().build()
    }
}
