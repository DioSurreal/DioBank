package com.diobank.ledger.application.service;

import com.diobank.ledger.application.exception.AccountNotFoundException;
import com.diobank.ledger.application.port.in.PostEntryUseCase;
import com.diobank.ledger.application.port.in.command.PostEntryCommand;
import com.diobank.ledger.application.port.in.result.PostEntryResult;
import com.diobank.ledger.application.port.out.AccountBalancePort;
import com.diobank.ledger.application.port.out.LedgerRepositoryPort;
import com.diobank.ledger.domain.model.AccountBalance;
import com.diobank.ledger.domain.model.Direction;
import com.diobank.ledger.domain.model.DoubleEntry;
import com.diobank.ledger.domain.model.LedgerEntry;
import com.diobank.ledger.domain.model.Money;
import com.diobank.ledger.domain.policy.PostingPolicy;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class LedgerTransactionService implements PostEntryUseCase {

    private final AccountBalancePort accountBalancePort;
    private final LedgerRepositoryPort ledgerRepositoryPort;

    public LedgerTransactionService(AccountBalancePort accountBalancePort, LedgerRepositoryPort ledgerRepositoryPort) {
        this.accountBalancePort = accountBalancePort;
        this.ledgerRepositoryPort = ledgerRepositoryPort;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PostEntryResult postEntry(PostEntryCommand command) {
        // 1. Fail-Fast Validation (Before hitting DB)
        if (command.debitAccountId().equals(command.creditAccountId())) {
            throw new IllegalArgumentException("Self-transfers are not allowed.");
        }
        if (command.amount() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly greater than 0");
        }

        // 2. Create Entries & Apply Domain Policy (In Memory)
        Money amountToTransfer = Money.of(command.amount());

        LedgerEntry debitEntry = LedgerEntry.create(
                command.transactionId(), command.debitAccountId(), Direction.DEBIT, amountToTransfer);

        LedgerEntry creditEntry = LedgerEntry.create(
                command.transactionId(), command.creditAccountId(), Direction.CREDIT, amountToTransfer);

        DoubleEntry doubleEntry = new DoubleEntry(debitEntry, creditEntry);
        PostingPolicy.validate(doubleEntry);

        // 3. Database-driven Idempotency (ON CONFLICT DO NOTHING) - Shields Account Locks!
        if (!ledgerRepositoryPort.saveDoubleEntryIfAbsent(doubleEntry)) {
            return new PostEntryResult(command.transactionId(), true);
        }

        // 4. Lock Ordering (Deadlock Prevention)
        UUID firstLock = command.debitAccountId();
        UUID secondLock = command.creditAccountId();

        if (firstLock.compareTo(secondLock) > 0) {
            firstLock = command.creditAccountId();
            secondLock = command.debitAccountId();
        }

        final UUID finalFirstLock = firstLock;
        final UUID finalSecondLock = secondLock;

        // 5. Acquire Locks using SELECT FOR UPDATE
        AccountBalance firstAccount = accountBalancePort.loadForUpdate(finalFirstLock)
                .orElseThrow(() -> new AccountNotFoundException(finalFirstLock));
        AccountBalance secondAccount = accountBalancePort.loadForUpdate(finalSecondLock)
                .orElseThrow(() -> new AccountNotFoundException(finalSecondLock));

        AccountBalance debitAccount = finalFirstLock.equals(command.debitAccountId()) ? firstAccount : secondAccount;
        AccountBalance creditAccount = finalSecondLock.equals(command.creditAccountId()) ? secondAccount : firstAccount;

        // 6. Mutate Balances (Validates Sufficient Funds Internally)
        debitAccount.deduct(amountToTransfer);
        creditAccount.add(amountToTransfer);

        // 7. Save State
        accountBalancePort.updateBalance(debitAccount);
        accountBalancePort.updateBalance(creditAccount);

        return new PostEntryResult(command.transactionId(), false);
    }
}
