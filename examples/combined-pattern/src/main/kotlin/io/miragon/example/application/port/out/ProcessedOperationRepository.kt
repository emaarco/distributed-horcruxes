package io.miragon.example.application.port.out

import io.miragon.example.domain.OperationId

interface ProcessedOperationRepository {
    fun existsById(operationId: OperationId): Boolean
    fun save(operationId: OperationId)
}
