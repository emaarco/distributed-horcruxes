package io.miragon.example.adapter.out.zeebe

import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_CONFIRMATION_REJECTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_MEMBERSHIP_CONFIRMED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Messages.MIRAVELO_REGISTRATION_SUBMITTED
import io.miragon.example.adapter.process.InnerCircleMembershipProcessApi.Variables
import io.miragon.example.application.port.out.MembershipProcess
import io.miragon.example.domain.MembershipId
import org.springframework.stereotype.Component

@Component
class MembershipProcessAdapter(
    private val engineApi: ProcessEngineApi
) : MembershipProcess {

    override fun submitRegistration(id: MembershipId) {
        val variables = mapOf(Variables.StartEventRegistrationSubmitted.MEMBERSHIP_ID.value to id.value.toString())
        engineApi.startProcessViaMessage(
            messageName = MIRAVELO_REGISTRATION_SUBMITTED.value,
            correlationId = id.value.toString(),
            variables = variables
        )
    }

    override fun confirmMembership(id: MembershipId) {
        engineApi.sendMessage(
            messageName = MIRAVELO_MEMBERSHIP_CONFIRMED.value,
            correlationId = id.value.toString(),
        )
    }

    override fun rejectConfirmation(id: MembershipId) {
        engineApi.sendMessage(
            messageName = MIRAVELO_CONFIRMATION_REJECTED.value,
            correlationId = id.value.toString(),
        )
    }
}
