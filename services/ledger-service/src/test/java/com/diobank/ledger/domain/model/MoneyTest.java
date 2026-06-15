package com.diobank.ledger.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money")
class MoneyTest {

    @Test
    @DisplayName("of() creates Money with positive amount")
    void of_positive_succeeds() {
        Money money = Money.of(1000L);
        assertThat(money.amount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("of() with zero amount succeeds (zero balance is valid)")
    void of_zero_succeeds() {
        Money money = Money.of(0L);
        assertThat(money.amount()).isZero();
    }

    @Test
    @DisplayName("of() with negative amount throws IllegalArgumentException")
    void of_negative_throws() {
        assertThatThrownBy(() -> Money.of(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("plus() adds two Money amounts correctly")
    void plus_addsTwoAmounts() {
        Money a = Money.of(100L);
        Money b = Money.of(50L);
        assertThat(a.plus(b).amount()).isEqualTo(150L);
    }

    @Test
    @DisplayName("minus() subtracts correctly when sufficient")
    void minus_subtractsSufficientAmount() {
        Money a = Money.of(100L);
        Money b = Money.of(40L);
        assertThat(a.minus(b).amount()).isEqualTo(60L);
    }

    @Test
    @DisplayName("minus() throws when subtracting more than available")
    void minus_throwsWhenInsufficient() {
        Money a = Money.of(50L);
        Money b = Money.of(100L);
        assertThatThrownBy(() -> a.minus(b))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ZERO constant has amount of 0")
    void zero_constant_isZero() {
        assertThat(Money.ZERO.amount()).isZero();
    }
}
