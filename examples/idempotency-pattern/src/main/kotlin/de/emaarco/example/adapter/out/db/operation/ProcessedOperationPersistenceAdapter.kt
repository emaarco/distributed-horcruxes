package de.emaarco.example.adapter.out.db.operation

import de.emaarco.example.application.port.out.ProcessedOperationRepository
import org.springframework.stereotype.Component

@Component
class ProcessedOperationPersistenceAdapter(
    private val repository: ProcessedOperationJpaRepository
) : ProcessedOperationRepository {

    override fun existsById(operationId: String): Boolean {
        return repository.existsById(operationId)
    }

    override fun save(operationId: String) {
        val entity = ProcessedOperationEntity(operationId = operationId)
        repository.save(entity)
    }
}
