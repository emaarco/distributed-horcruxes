package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendConfirmationMailUseCase
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SendConfirmationMailService(
    private val repository: MembershipRepository,
) : SendConfirmationMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendConfirmationMail(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        log.info { "Sending confirmation mail to ${membership.email.value}" }
    }
}
