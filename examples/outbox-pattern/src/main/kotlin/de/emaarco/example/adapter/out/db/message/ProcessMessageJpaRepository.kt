package de.emaarco.example.adapter.out.db.message

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints

interface ProcessMessageJpaRepository : JpaRepository<ProcessMessageEntity, String> {
    fun findAllByStatusOrderByCreatedAtAsc(status: MessageStatus): List<ProcessMessageEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    @Query("SELECT m FROM process_message m WHERE m.status = :status ORDER BY m.createdAt ASC")
    fun findFirstByStatusWithLock(status: MessageStatus): ProcessMessageEntity?
}