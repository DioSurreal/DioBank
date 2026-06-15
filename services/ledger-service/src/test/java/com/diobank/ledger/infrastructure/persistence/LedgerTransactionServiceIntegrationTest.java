package com.diobank.ledger.infrastructure.persistence;

import com.diobank.ledger.application.port.in.command.PostEntryCommand;
import com.diobank.ledger.application.port.in.result.PostEntryResult;
import com.diobank.ledger.application.service.LedgerTransactionService;
import com.diobank.ledger.domain.exception.InsufficientBalanceException;
import com.diobank.ledger.infrastructure.persistence.adapter.AccountBalanceRepositoryAdapter;
import com.diobank.ledger.infrastructure.persistence.adapter.LedgerRepositoryAdapter;
import com.diobank.ledger.infrastructure.persistence.repository.AccountBalanceJdbcRepository;
import com.diobank.ledger.infrastructure.persistence.repository.LedgerEntryJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("LedgerTransactionService Integration Tests")
class LedgerTransactionServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ledger_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired AccountBalanceJdbcRepository accountBalanceRepo;
    @Autowired LedgerEntryJdbcRepository ledgerEntryRepo;
    @Autowired JdbcTemplate jdbcTemplate;

    LedgerTransactionService service;
    AccountBalanceRepositoryAdapter accountBalanceAdapter;
    LedgerRepositoryAdapter ledgerAdapter;

    private static final long INITIAL_BALANCE = 10_000L; // 100 THB in satang

    @BeforeEach
    void setUp() {
        accountBalanceAdapter = new AccountBalanceRepositoryAdapter(accountBalanceRepo);
        ledgerAdapter = new LedgerRepositoryAdapter(ledgerEntryRepo);
        service = new LedgerTransactionService(accountBalanceAdapter, ledgerAdapter);

        // Clean slate for each test
        jdbcTemplate.execute("DELETE FROM ledger_entries");
        jdbcTemplate.execute("DELETE FROM account_balance");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private UUID createAccount(long initialBalance) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO account_balance (account_id, balance, updated_at) VALUES (?, ?, NOW())",
                id, initialBalance);
        return id;
    }

    private long getBalance(UUID accountId) {
        return jdbcTemplate.queryForObject(
                "SELECT balance FROM account_balance WHERE account_id = ?",
                Long.class, accountId);
    }

    private long countLedgerEntries(UUID transactionId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ledger_entries WHERE transaction_id = ?",
                Long.class, transactionId);
    }

    // ── Happy Path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("postEntry() successfully transfers amount and persists 2 ledger entries")
    void postEntry_happyPath_persistsAll() {
        UUID alice = createAccount(INITIAL_BALANCE);
        UUID bob = createAccount(0L);
        UUID txId = UUID.randomUUID();

        PostEntryResult result = service.postEntry(new PostEntryCommand(txId, alice, bob, 3_000L));

        assertThat(result.alreadyExisted()).isFalse();
        assertThat(getBalance(alice)).isEqualTo(7_000L);
        assertThat(getBalance(bob)).isEqualTo(3_000L);
        assertThat(countLedgerEntries(txId)).isEqualTo(2L);
    }

    // ── Idempotency ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("postEntry() with same transaction_id twice does not mutate balance on second call")
    void postEntry_sameTransactionId_idempotent() {
        UUID alice = createAccount(INITIAL_BALANCE);
        UUID bob = createAccount(0L);
        UUID txId = UUID.randomUUID();
        PostEntryCommand command = new PostEntryCommand(txId, alice, bob, 3_000L);

        // First call — should succeed normally
        PostEntryResult first = service.postEntry(command);
        assertThat(first.alreadyExisted()).isFalse();

        // Second call — same txId, should short-circuit
        PostEntryResult second = service.postEntry(command);
        assertThat(second.alreadyExisted()).isTrue();

        // Balance must remain as after first call only
        assertThat(getBalance(alice)).isEqualTo(7_000L);
        assertThat(getBalance(bob)).isEqualTo(3_000L);
        assertThat(countLedgerEntries(txId)).isEqualTo(2L); // Still exactly 2 entries
    }

    // ── Rollback ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("postEntry() rolls back ledger entries when balance is insufficient")
    void postEntry_insufficientFunds_rollsBackLedgerEntries() {
        UUID alice = createAccount(100L);
        UUID bob = createAccount(0L);
        UUID txId = UUID.randomUUID();

        assertThatThrownBy(() -> service.postEntry(new PostEntryCommand(txId, alice, bob, 200L)))
                .isInstanceOf(InsufficientBalanceException.class);

        // Balance must be unchanged
        assertThat(getBalance(alice)).isEqualTo(100L);
        assertThat(getBalance(bob)).isZero();

        // Critical: ledger_entries must be rolled back — no orphaned entries allowed
        assertThat(countLedgerEntries(txId)).isZero();
    }

    // ── Concurrency: Duplicate Detection ─────────────────────────────────────

    @Test
    @DisplayName("concurrent duplicate postEntry() calls — only one persists, no double-spend")
    void postEntry_concurrentDuplicates_onlyOneWins() throws Exception {
        UUID alice = createAccount(INITIAL_BALANCE);
        UUID bob = createAccount(0L);
        UUID txId = UUID.randomUUID();
        PostEntryCommand command = new PostEntryCommand(txId, alice, bob, 1_000L);

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<PostEntryResult>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> service.postEntry(command)));
        }
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        long successCount = futures.stream()
                .map(f -> {
                    try { return f.get(); } catch (Exception e) { return null; }
                })
                .filter(r -> r != null && !r.alreadyExisted())
                .count();

        // Exactly one call must be the "real" one
        assertThat(successCount).isEqualTo(1L);
        // Balance must reflect exactly one transfer
        assertThat(getBalance(alice)).isEqualTo(9_000L);
        assertThat(getBalance(bob)).isEqualTo(1_000L);
        // Exactly 2 ledger entries, not 20
        assertThat(countLedgerEntries(txId)).isEqualTo(2L);
    }

    // ── Concurrency: Deadlock Prevention ─────────────────────────────────────

    @Test
    @DisplayName("concurrent opposing transfers (A→B and B→A) complete without deadlock")
    void postEntry_concurrentOpposingTransfers_noDeadlock() throws Exception {
        UUID alice = createAccount(INITIAL_BALANCE);
        UUID bob = createAccount(INITIAL_BALANCE);

        UUID txAtoB = UUID.randomUUID();
        UUID txBtoA = UUID.randomUUID();

        int rounds = 5;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<PostEntryResult>> futures = new ArrayList<>();

        for (int i = 0; i < rounds; i++) {
            UUID atob = UUID.randomUUID();
            UUID btoa = UUID.randomUUID();
            futures.add(executor.submit(() -> service.postEntry(new PostEntryCommand(atob, alice, bob, 100L))));
            futures.add(executor.submit(() -> service.postEntry(new PostEntryCommand(btoa, bob, alice, 100L))));
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);

        // Must complete without hanging
        assertThat(finished).isTrue();

        // All futures must resolve (no deadlock-induced hangs left behind)
        for (Future<PostEntryResult> future : futures) {
            assertThatCode(future::get).doesNotThrowAnyException();
        }

        // Net balance must be preserved (10 transfers each way = net zero shift)
        assertThat(getBalance(alice) + getBalance(bob)).isEqualTo(INITIAL_BALANCE * 2);
    }
}
