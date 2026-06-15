package com.diobank.ledger.domain.policy;

import com.diobank.ledger.domain.exception.InvalidPostingException;
import com.diobank.ledger.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PostingPolicy")
class PostingPolicyTest {

    private static final UUID TX_ID = UUID.randomUUID();
    private static final UUID DEBIT_ACCOUNT = UUID.randomUUID();
    private static final UUID CREDIT_ACCOUNT = UUID.randomUUID();

    @Test
    @DisplayName("validate() passes for a valid balanced DoubleEntry")
    void validate_validDoubleEntry_passes() {
        DoubleEntry entry = buildEntry(TX_ID, DEBIT_ACCOUNT, CREDIT_ACCOUNT, 500L);
        assertThatNoException().isThrownBy(() -> PostingPolicy.validate(entry));
    }

    @Test
    @DisplayName("validate() throws when debit and credit amounts differ")
    void validate_unequalAmounts_throws() {
        // Force unequal by building entries manually
        LedgerEntry debit = new LedgerEntry(UUID.randomUUID(), TX_ID, DEBIT_ACCOUNT,
                Direction.DEBIT, Money.of(500L), Instant.now());
        LedgerEntry credit = new LedgerEntry(UUID.randomUUID(), TX_ID, CREDIT_ACCOUNT,
                Direction.CREDIT, Money.of(300L), Instant.now());
        DoubleEntry entry = new DoubleEntry(debit, credit);

        assertThatThrownBy(() -> PostingPolicy.validate(entry))
                .isInstanceOf(InvalidPostingException.class)
                .hasMessageContaining("equal");
    }

    @Test
    @DisplayName("validate() throws when debit and credit are the same account")
    void validate_sameAccount_throws() {
        LedgerEntry debit = new LedgerEntry(UUID.randomUUID(), TX_ID, DEBIT_ACCOUNT,
                Direction.DEBIT, Money.of(500L), Instant.now());
        LedgerEntry credit = new LedgerEntry(UUID.randomUUID(), TX_ID, DEBIT_ACCOUNT, // same account
                Direction.CREDIT, Money.of(500L), Instant.now());
        DoubleEntry entry = new DoubleEntry(debit, credit);

        assertThatThrownBy(() -> PostingPolicy.validate(entry))
                .isInstanceOf(InvalidPostingException.class)
                .hasMessageContaining("Self-transfer");
    }

    @Test
    @DisplayName("DoubleEntry constructor throws when directions are swapped")
    void doubleEntry_swappedDirections_throws() {
        LedgerEntry credit = new LedgerEntry(UUID.randomUUID(), TX_ID, CREDIT_ACCOUNT,
                Direction.CREDIT, Money.of(500L), Instant.now());
        LedgerEntry debit = new LedgerEntry(UUID.randomUUID(), TX_ID, DEBIT_ACCOUNT,
                Direction.DEBIT, Money.of(500L), Instant.now());

        // Swapped: credit first, debit second
        assertThatThrownBy(() -> new DoubleEntry(credit, debit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEBIT");
    }

    @Test
    @DisplayName("DoubleEntry constructor throws when entries belong to different transactions")
    void doubleEntry_differentTransactionIds_throws() {
        LedgerEntry debit = new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(), DEBIT_ACCOUNT,
                Direction.DEBIT, Money.of(500L), Instant.now());
        LedgerEntry credit = new LedgerEntry(UUID.randomUUID(), UUID.randomUUID(), CREDIT_ACCOUNT,
                Direction.CREDIT, Money.of(500L), Instant.now());

        assertThatThrownBy(() -> new DoubleEntry(debit, credit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same transaction");
    }

    // Helper
    private DoubleEntry buildEntry(UUID txId, UUID debitAccount, UUID creditAccount, long amount) {
        LedgerEntry debit = new LedgerEntry(UUID.randomUUID(), txId, debitAccount,
                Direction.DEBIT, Money.of(amount), Instant.now());
        LedgerEntry credit = new LedgerEntry(UUID.randomUUID(), txId, creditAccount,
                Direction.CREDIT, Money.of(amount), Instant.now());
        return new DoubleEntry(debit, credit);
    }
}
