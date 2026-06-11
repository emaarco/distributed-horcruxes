package io.miragon.example.domain

data class SubscriptionCounter(
    val count: Int
) {
    fun increment(): SubscriptionCounter = copy(count = count + 1)
}
