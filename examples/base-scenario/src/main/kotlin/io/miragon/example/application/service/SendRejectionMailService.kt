package io.miragon.example.application.service

import io.miragon.example.application.port.`in`.SendRejectionMailUseCase
import io.miragon.example.application.port.out.MembershipRepository
import io.miragon.example.domain.MembershipId
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SendRejectionMailService(
    private val repository: MembershipRepository,
) : SendRejectionMailUseCase {

    private val log = KotlinLogging.logger {}

    override fun sendRejectionMail(membershipId: MembershipId) {
        val membership = repository.find(membershipId)
        log.info { "Sending rejection mail to ${membership.email.value}" }
    }
}
