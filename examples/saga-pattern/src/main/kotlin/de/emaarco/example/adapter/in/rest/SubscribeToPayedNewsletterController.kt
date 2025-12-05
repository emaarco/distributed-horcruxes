package de.emaarco.example.adapter.`in`.rest

import de.emaarco.example.application.port.`in`.SubscribeToPayedNewsletterUseCase
import de.emaarco.example.domain.Email
import de.emaarco.example.domain.Name
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payed-newsletter")
class SubscribeToPayedNewsletterController(
    private val useCase: SubscribeToPayedNewsletterUseCase
) {

    @PostMapping("/subscribe")
    fun subscribe(@RequestBody request: SubscribeRequest): ResponseEntity<SubscribeResponse> {
        val command = request.toCommand()
        val subscriptionId = useCase.subscribe(command)
        return ResponseEntity.ok(SubscribeResponse(subscriptionId.value.toString()))
    }

    private fun SubscribeRequest.toCommand() = SubscribeToPayedNewsletterUseCase.Command(
        email = Email(this.email),
        name = Name(this.name)
    )

    data class SubscribeRequest(
        val email: String,
        val name: String
    )

    data class SubscribeResponse(
        val subscriptionId: String
    )
}
