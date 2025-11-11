package de.emaarco.example.adapter.out.db.operation

import de.emaarco.example.application.port.out.ProcessedOperationRepository
import de.emaarco.example.domain.OperationId
import org.springframework.stereotype.Component

@Component
class ProcessedOperationPersistenceAdapter(
    private val repository: ProcessedOperationJpaRepository
) : ProcessedOperationRepository {

    override fun existsById(operationId: OperationId): Boolean {
        return repository.existsById(operationId.value)
    }

    override fun save(operationId: OperationId) {
        val entity = ProcessedOperationEntity(operationId = operationId.value)
        repository.save(entity)
    }
}
