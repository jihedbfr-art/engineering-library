package com.jihedapps.financedashboard.repository;

import com.jihedapps.financedashboard.entity.Budget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @BeforeEach
    void setUp() {
        budgetRepository.save(new Budget("groceries", new BigDecimal("400.00")));
    }

    @Test
    void findByCategoryReturnsTheMatchingBudget() {
        Optional<Budget> found = budgetRepository.findByCategory("groceries");

        assertThat(found).isPresent();
        assertThat(found.get().getMonthlyLimit()).isEqualByComparingTo("400.00");
    }

    @Test
    void findByCategoryIsEmptyWhenNoBudgetExistsForIt() {
        Optional<Budget> found = budgetRepository.findByCategory("entertainment");

        assertThat(found).isEmpty();
    }
}
