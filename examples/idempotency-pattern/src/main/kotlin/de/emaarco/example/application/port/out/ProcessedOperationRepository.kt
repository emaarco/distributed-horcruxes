package de.emaarco.example.application.port.out

interface ProcessedOperationRepository {
    fun existsById(operationId: String): Boolean
    fun save(operationId: String)
}
