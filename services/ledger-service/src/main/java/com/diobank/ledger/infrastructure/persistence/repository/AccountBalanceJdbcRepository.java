package com.diobank.ledger.infrastructure.persistence.repository;

import com.diobank.ledger.infrastructure.persistence.entity.AccountBalanceEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountBalanceJdbcRepository extends CrudRepository<AccountBalanceEntity, UUID> {
    
    @Query("SELECT * FROM account_balance WHERE account_id = :accountId FOR UPDATE")
    Optional<AccountBalanceEntity> findByIdForUpdate(@Param("accountId") UUID accountId);

    @Modifying
    @Query("INSERT INTO account_balance (account_id, balance, updated_at) VALUES (:accountId, 0, NOW()) ON CONFLICT (account_id) DO NOTHING")
    int insertIfAbsent(@Param("accountId") UUID accountId);
}
