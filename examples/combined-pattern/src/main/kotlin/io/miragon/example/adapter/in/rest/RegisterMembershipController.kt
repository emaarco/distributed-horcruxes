package io.miragon.example.adapter.`in`.rest

import io.miragon.example.application.port.`in`.RegisterMembershipUseCase
import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/memberships")
class RegisterMembershipController(private val useCase: RegisterMembershipUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/register")
    fun registerMembership(@RequestBody input: RegistrationForm): ResponseEntity<Response> {
        log.debug { "Received REST-request to register for the inner circle: $input" }
        val membershipId = useCase.register(input.toCommand())
        return ResponseEntity.ok().body(Response(membershipId.value.toString()))
    }

    data class RegistrationForm(
        val email: String,
        val name: String
    )

    data class Response(val membershipId: String)

    private fun RegistrationForm.toCommand() = RegisterMembershipUseCase.Command(
        Email(email),
        Name(name)
    )
}
