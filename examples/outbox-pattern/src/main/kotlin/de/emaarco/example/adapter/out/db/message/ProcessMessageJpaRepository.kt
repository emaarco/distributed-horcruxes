package de.emaarco.example.adapter.out.db.message

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessMessageJpaRepository : JpaRepository<ProcessMessageEntity, String> {
    fun findAllByStatusOrderByCreatedAtAsc(status: MessageStatus): List<ProcessMessageEntity>
}