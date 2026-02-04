package io.miragon.example.adapter.`in`.rest

import io.miragon.example.application.port.`in`.ConfirmSubscriptionUseCase
import io.miragon.example.domain.SubscriptionId
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/subscriptions")
class ConfirmSubscriptionController(private val useCase: ConfirmSubscriptionUseCase) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/confirm/{subscriptionId}")
    fun confirmSubscription(@PathVariable subscriptionId: String): ResponseEntity<Void> {
        log.debug { "Received REST-request to confirm subscription: $subscriptionId" }
        useCase.confirm(SubscriptionId(UUID.fromString(subscriptionId)))
        return ResponseEntity.ok().build()
    }
}
