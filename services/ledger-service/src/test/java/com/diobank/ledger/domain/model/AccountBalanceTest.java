package com.diobank.ledger.domain.model;

import com.diobank.ledger.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountBalance")
class AccountBalanceTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Test
    @DisplayName("createInitial() creates account with zero balance")
    void createInitial_hasZeroBalance() {
        AccountBalance account = AccountBalance.createInitial(ACCOUNT_ID);
        assertThat(account.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(account.getBalance().amount()).isZero();
    }

    @Test
    @DisplayName("deduct() reduces balance by given amount")
    void deduct_reducesBalance() {
        AccountBalance account = new AccountBalance(ACCOUNT_ID, Money.of(1000L), Instant.now());
        account.deduct(Money.of(300L));
        assertThat(account.getBalance().amount()).isEqualTo(700L);
    }

    @Test
    @DisplayName("deduct() throws InsufficientBalanceException when funds are not enough")
    void deduct_throwsWhenInsufficient() {
        AccountBalance account = new AccountBalance(ACCOUNT_ID, Money.of(100L), Instant.now());
        assertThatThrownBy(() -> account.deduct(Money.of(101L)))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining(ACCOUNT_ID.toString());
    }

    @Test
    @DisplayName("deduct() throws when deducting exactly more than zero balance")
    void deduct_throwsOnZeroBalance() {
        AccountBalance account = new AccountBalance(ACCOUNT_ID, Money.ZERO, Instant.now());
        assertThatThrownBy(() -> account.deduct(Money.of(1L)))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("add() increases balance by given amount")
    void add_increasesBalance() {
        AccountBalance account = new AccountBalance(ACCOUNT_ID, Money.of(500L), Instant.now());
        account.add(Money.of(250L));
        assertThat(account.getBalance().amount()).isEqualTo(750L);
    }

    @Test
    @DisplayName("constructor throws NullPointerException for null accountId")
    void constructor_throwsForNullAccountId() {
        assertThatThrownBy(() -> new AccountBalance(null, Money.ZERO, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }
}
