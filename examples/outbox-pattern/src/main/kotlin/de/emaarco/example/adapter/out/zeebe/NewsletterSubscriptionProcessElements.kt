package de.emaarco.example.adapter.out.zeebe

object NewsletterSubscriptionProcessElements {
    val PROCESS_ID = "newsletter-subscription"
    val START_EVENT_FORM_SUBMITTED = "StartEvent_SubmitRegistrationForm";
    val MESSAGE_FORM_SUBMITTED = "Message_FormSubmitted"
    val TASK_SEND_CONFIRMATION_MAIL = "Activity_SendConfirmationMail"
    val TASK_RECEIVE_CONFIRMATION = "Activity_ConfirmRegistration"
    val MESSAGE_RECEIVE_CONFIRMATION = "Message_SubscriptionConfirmed"
    val TASK_SEND_WELCOME_MAIL = "Activity_SendWelcomeMail"
    val TASK_ABORT_REGISTRATION = "Activity_AbortRegistration"
    val END_EVENT_REGISTRATION_COMPLETED = "EndEvent_RegistrationCompleted"
    val END_EVENT_REGISTRATION_ABORTED = "EndEvent_RegistrationAborted"
}