package com.diobank.ledger.application.service;

import com.diobank.ledger.application.exception.AccountNotFoundException;
import com.diobank.ledger.application.port.in.command.PostEntryCommand;
import com.diobank.ledger.application.port.in.result.PostEntryResult;
import com.diobank.ledger.application.port.out.AccountBalancePort;
import com.diobank.ledger.application.port.out.LedgerRepositoryPort;
import com.diobank.ledger.domain.exception.InsufficientBalanceException;
import com.diobank.ledger.domain.model.AccountBalance;
import com.diobank.ledger.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerTransactionService")
class LedgerTransactionServiceTest {

    @Mock private AccountBalancePort accountBalancePort;
    @Mock private LedgerRepositoryPort ledgerRepositoryPort;

    private LedgerTransactionService service;

    private static final UUID TX_ID = UUID.randomUUID();
    private static final UUID DEBIT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CREDIT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        service = new LedgerTransactionService(accountBalancePort, ledgerRepositoryPort);
    }

    // --- Fast Path Idempotency ---

    @Test
    @DisplayName("postEntry() returns alreadyExisted=true when ledger entry already exists (idempotent)")
    void postEntry_duplicate_returnsAlreadyExisted() {
        // saveDoubleEntryIfAbsent returns false = conflict, already exists
        when(ledgerRepositoryPort.saveDoubleEntryIfAbsent(any())).thenReturn(false);

        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, 500L);
        PostEntryResult result = service.postEntry(command);

        assertThat(result.isIdempotentFallback()).isTrue();
        assertThat(result.transactionId()).isEqualTo(TX_ID);

        // No account locks or mutations should occur
        verifyNoInteractions(accountBalancePort);
    }

    // --- Happy Path ---

    @Test
    @DisplayName("postEntry() succeeds and returns alreadyExisted=false on first call")
    void postEntry_newTransaction_succeeds() {
        AccountBalance debit = new AccountBalance(DEBIT_ID, Money.of(1000L), Instant.now());
        AccountBalance credit = new AccountBalance(CREDIT_ID, Money.of(0L), Instant.now());

        when(ledgerRepositoryPort.saveDoubleEntryIfAbsent(any())).thenReturn(true);
        // DEBIT_ID < CREDIT_ID lexicographically, so firstLock=DEBIT_ID, secondLock=CREDIT_ID
        when(accountBalancePort.loadForUpdate(DEBIT_ID)).thenReturn(Optional.of(debit));
        when(accountBalancePort.loadForUpdate(CREDIT_ID)).thenReturn(Optional.of(credit));

        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, 300L);
        PostEntryResult result = service.postEntry(command);

        assertThat(result.isIdempotentFallback()).isFalse();
        assertThat(result.transactionId()).isEqualTo(TX_ID);

        // Balances should be updated for both accounts
        verify(accountBalancePort, times(2)).updateBalance(any());
    }

    // --- Fail-Fast Validation ---

    @Test
    @DisplayName("postEntry() throws IllegalArgumentException for self-transfer")
    void postEntry_selfTransfer_throws() {
        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, DEBIT_ID, 100L);

        assertThatThrownBy(() -> service.postEntry(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Self-transfer");

        verifyNoInteractions(ledgerRepositoryPort, accountBalancePort);
    }

    @Test
    @DisplayName("postEntry() throws IllegalArgumentException for zero amount")
    void postEntry_zeroAmount_throws() {
        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, 0L);

        assertThatThrownBy(() -> service.postEntry(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than 0");

        verifyNoInteractions(ledgerRepositoryPort, accountBalancePort);
    }

    @Test
    @DisplayName("postEntry() throws IllegalArgumentException for negative amount")
    void postEntry_negativeAmount_throws() {
        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, -1L);

        assertThatThrownBy(() -> service.postEntry(command))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(ledgerRepositoryPort, accountBalancePort);
    }

    // --- Error Cases ---

    @Test
    @DisplayName("postEntry() throws AccountNotFoundException when debit account does not exist")
    void postEntry_debitAccountNotFound_throws() {
        when(ledgerRepositoryPort.saveDoubleEntryIfAbsent(any())).thenReturn(true);
        when(accountBalancePort.loadForUpdate(any())).thenReturn(Optional.empty());

        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, 100L);

        assertThatThrownBy(() -> service.postEntry(command))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("postEntry() throws InsufficientBalanceException when debit account has insufficient funds")
    void postEntry_insufficientFunds_throws() {
        AccountBalance debit = new AccountBalance(DEBIT_ID, Money.of(50L), Instant.now());
        AccountBalance credit = new AccountBalance(CREDIT_ID, Money.of(0L), Instant.now());

        when(ledgerRepositoryPort.saveDoubleEntryIfAbsent(any())).thenReturn(true);
        when(accountBalancePort.loadForUpdate(DEBIT_ID)).thenReturn(Optional.of(debit));
        when(accountBalancePort.loadForUpdate(CREDIT_ID)).thenReturn(Optional.of(credit));

        PostEntryCommand command = new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, 100L);

        assertThatThrownBy(() -> service.postEntry(command))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    // --- Balance Mutation Verification ---

    @Test
    @DisplayName("postEntry() correctly adjusts debit and credit balances")
    void postEntry_correctlyMutatesBalances() {
        AccountBalance debit = new AccountBalance(DEBIT_ID, Money.of(1000L), Instant.now());
        AccountBalance credit = new AccountBalance(CREDIT_ID, Money.of(200L), Instant.now());

        when(ledgerRepositoryPort.saveDoubleEntryIfAbsent(any())).thenReturn(true);
        when(accountBalancePort.loadForUpdate(DEBIT_ID)).thenReturn(Optional.of(debit));
        when(accountBalancePort.loadForUpdate(CREDIT_ID)).thenReturn(Optional.of(credit));

        service.postEntry(new PostEntryCommand(TX_ID, DEBIT_ID, CREDIT_ID, 400L));

        assertThat(debit.getBalance().amount()).isEqualTo(600L);
        assertThat(credit.getBalance().amount()).isEqualTo(600L);
    }
}
