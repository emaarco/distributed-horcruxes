package de.emaarco.example.domain

import java.util.UUID

data class SubscriptionId(val value: UUID = UUID.randomUUID())
