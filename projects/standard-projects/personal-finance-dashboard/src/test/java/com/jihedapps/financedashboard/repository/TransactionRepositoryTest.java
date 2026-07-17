package com.jihedapps.financedashboard.repository;

import com.jihedapps.financedashboard.entity.Transaction;
import com.jihedapps.financedashboard.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.save(new Transaction(new BigDecimal("50.00"), "groceries",
                LocalDate.of(2026, 2, 28), TransactionType.EXPENSE, "end of feb"));
        transactionRepository.save(new Transaction(new BigDecimal("100.00"), "groceries",
                LocalDate.of(2026, 3, 15), TransactionType.EXPENSE, "in march"));
        transactionRepository.save(new Transaction(new BigDecimal("30.00"), "transport",
                LocalDate.of(2026, 3, 20), TransactionType.EXPENSE, "in march"));
        transactionRepository.save(new Transaction(new BigDecimal("1500.00"), "salary",
                LocalDate.of(2026, 4, 1), TransactionType.INCOME, "start of april"));
    }

    @Test
    void findByDateBetweenIsInclusiveOnBothEnds() {
        List<Transaction> march = transactionRepository.findByDateBetween(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(march).extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("in march", "in march");
        assertThat(march).hasSize(2);
    }

    @Test
    void findByCategoryAndDateBetweenNarrowsToOneCategory() {
        List<Transaction> groceriesInMarch = transactionRepository.findByCategoryAndDateBetween(
                "groceries", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(groceriesInMarch).hasSize(1);
        assertThat(groceriesInMarch.get(0).getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void findByDateBetweenExcludesTransactionsOutsideRange() {
        List<Transaction> march = transactionRepository.findByDateBetween(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(march).noneMatch(t -> t.getCategory().equals("salary"));
    }
}
