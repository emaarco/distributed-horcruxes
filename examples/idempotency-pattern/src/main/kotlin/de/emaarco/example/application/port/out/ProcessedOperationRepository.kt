package de.emaarco.example.application.port.out

import de.emaarco.example.domain.OperationId

interface ProcessedOperationRepository {
    fun existsById(operationId: OperationId): Boolean
    fun save(operationId: OperationId)
}
