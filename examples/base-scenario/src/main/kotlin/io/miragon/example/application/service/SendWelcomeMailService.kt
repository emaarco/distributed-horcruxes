package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendWelcomeMailUseCase
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SendWelcomeMailService(
    private val repository: MembershipRepository,
) : SendWelcomeMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendWelcomeMail(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        log.info { "Sending welcome mail to ${membership.email.value}" }
    }
}
