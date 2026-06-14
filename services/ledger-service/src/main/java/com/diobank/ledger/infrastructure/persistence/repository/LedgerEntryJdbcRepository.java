package com.diobank.ledger.infrastructure.persistence.repository;

import com.diobank.ledger.infrastructure.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.time.Instant;

public interface LedgerEntryJdbcRepository extends CrudRepository<LedgerEntryEntity, UUID> {
    
    @Modifying
    @Query("INSERT INTO ledger_entries (ledger_id, transaction_id, account_id, direction, amount, created_at) VALUES (:ledgerId, :transactionId, :accountId, :direction, :amount, :createdAt) ON CONFLICT (transaction_id, direction) DO NOTHING")
    int insertIfAbsent(@Param("ledgerId") UUID ledgerId, @Param("transactionId") UUID transactionId, @Param("accountId") UUID accountId, @Param("direction") String direction, @Param("amount") long amount, @Param("createdAt") Instant createdAt);
}
