package io.miragon.example.adapter.out.db.message

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints

interface ProcessMessageJpaRepository : JpaRepository<ProcessMessageEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    @Query("SELECT m FROM process_message m WHERE m.status = :status ORDER BY m.createdAt ASC")
    fun findByStatusWithLock(status: MessageStatus, pageable: Pageable): List<ProcessMessageEntity>

    fun findFirstByStatusWithLock(status: MessageStatus): ProcessMessageEntity? =
        findByStatusWithLock(status, PageRequest.of(0, 1)).firstOrNull()

}
