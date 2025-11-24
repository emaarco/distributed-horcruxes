package de.emaarco.example.domain

data class SubscriptionCounter(
    val count: Int
) {
    fun increment(): SubscriptionCounter = copy(count = count + 1)
}
