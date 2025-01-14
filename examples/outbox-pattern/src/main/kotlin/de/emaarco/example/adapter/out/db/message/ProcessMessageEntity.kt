package de.emaarco.example.adapter.out.db.message

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "process_message")
data class ProcessMessageEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", nullable = false)
    val messageId: String? = null,

    @Column(name = "message_name", nullable = false)
    val messageName: String,

    @Column(name = "correlation_id", nullable = true)
    val correlationId: String? = null,

    @Column(name = "variables", nullable = false)
    val variables: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

)