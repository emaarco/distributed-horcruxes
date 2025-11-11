package de.emaarco.example.adapter.out.db.operation

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedOperationJpaRepository : JpaRepository<ProcessedOperationEntity, String>
