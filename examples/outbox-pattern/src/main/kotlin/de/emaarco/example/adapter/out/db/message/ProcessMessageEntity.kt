package de.emaarco.example.adapter.out.db.message

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity(name = "process_message")
data class ProcessMessageEntity(

    @Id
    @Column(name = "message_id", nullable = false)
    val messageId: UUID = UUID.randomUUID(),

    @Column(name = "message_name", nullable = false)
    val messageName: String,

    @Column(name = "correlation_id", nullable = true)
    val correlationId: String? = null,

    @Column(name = "variables", nullable = false)
    val variables: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "retry_count", nullable = false)
    val retryCount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: MessageStatus = MessageStatus.PENDING

) {

    @PreUpdate
    fun updatedAt() {
        this.updatedAt = LocalDateTime.now()
    }
}