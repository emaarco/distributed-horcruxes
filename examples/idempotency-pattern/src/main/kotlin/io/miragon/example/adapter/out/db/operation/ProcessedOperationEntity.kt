package io.miragon.example.adapter.out.db.operation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "processed_operations")
data class ProcessedOperationEntity(
    @Id
    @Column(name = "operation_id", nullable = false)
    val operationId: String,

    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant = Instant.now()
)
