package de.emaarco.example.domain

data class PayedNewsletterSubscription(
    val id: SubscriptionId = SubscriptionId(),
    val email: Email,
    val name: Name,
    val paymentSuccessful: Boolean? = null
) {

    fun processPayment(
        paymentSuccessful: Boolean
    ) = this.copy(
        paymentSuccessful = paymentSuccessful
    )
}
