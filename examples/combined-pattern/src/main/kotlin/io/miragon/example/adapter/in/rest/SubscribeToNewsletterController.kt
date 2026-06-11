package io.miragon.example.adapter.`in`.rest

import io.miragon.example.application.port.`in`.SubscribeToNewsletterUseCase
import io.miragon.example.domain.Email
import io.miragon.example.domain.Name
import io.miragon.example.domain.NewsletterId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/subscriptions")
class SubscribeToNewsletterController(private val useCase: SubscribeToNewsletterUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/subscribe")
    fun subscribeToNewsletter(@RequestBody input: SubscriptionForm): ResponseEntity<Response> {
        log.debug { "Received REST-request to subscribe to newsletter: $input" }
        val subscriptionId = useCase.subscribe(input.toCommand())
        return ResponseEntity.ok().body(Response(subscriptionId.value.toString()))
    }

    data class SubscriptionForm(
        val email: String,
        val name: String,
        val newsletterId: String
    )

    data class Response(val subscriptionId: String)

    private fun SubscriptionForm.toCommand() = SubscribeToNewsletterUseCase.Command(
        Email(email),
        Name(name),
        NewsletterId(UUID.fromString(newsletterId))
    )
}
