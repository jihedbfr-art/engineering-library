package com.jihedapps.financedashboard.service;

import com.jihedapps.financedashboard.dto.CategorySummary;
import com.jihedapps.financedashboard.dto.MonthlySummary;
import com.jihedapps.financedashboard.entity.Budget;
import com.jihedapps.financedashboard.entity.Transaction;
import com.jihedapps.financedashboard.entity.TransactionType;
import com.jihedapps.financedashboard.repository.BudgetRepository;
import com.jihedapps.financedashboard.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        summaryService = new SummaryService(transactionRepository, budgetRepository);
    }

    private Transaction transaction(BigDecimal amount, String category, TransactionType type, int day) {
        return new Transaction(amount, category, LocalDate.of(2026, Month.MARCH, day), type, "note");
    }

    @Test
    void queriesTheWholeMonthRange() {
        when(transactionRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        summaryService.getMonthlySummary(2026, 3);

        org.mockito.Mockito.verify(transactionRepository)
                .findByDateBetween(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
    }

    @Test
    void computesTotalsAndBalanceAcrossCategories() {
        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("2000.00"), "salary", TransactionType.INCOME, 1),
                transaction(new BigDecimal("300.00"), "groceries", TransactionType.EXPENSE, 5),
                transaction(new BigDecimal("150.00"), "groceries", TransactionType.EXPENSE, 20),
                transaction(new BigDecimal("80.00"), "transport", TransactionType.EXPENSE, 10)
        );
        when(transactionRepository.findByDateBetween(any(), any())).thenReturn(transactions);
        when(budgetRepository.findByCategory(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        MonthlySummary summary = summaryService.getMonthlySummary(2026, 3);

        assertThat(summary.getYear()).isEqualTo(2026);
        assertThat(summary.getMonth()).isEqualTo(3);
        assertThat(summary.getTotalIncome()).isEqualByComparingTo("2000.00");
        assertThat(summary.getTotalExpense()).isEqualByComparingTo("530.00");
        assertThat(summary.getBalance()).isEqualByComparingTo("1470.00");
        assertThat(summary.getCategories()).hasSize(3);
    }

    @Test
    void flagsACategoryAsOverBudgetWhenExpenseExceedsTheLimit() {
        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("300.00"), "groceries", TransactionType.EXPENSE, 5),
                transaction(new BigDecimal("150.00"), "groceries", TransactionType.EXPENSE, 20)
        );
        when(transactionRepository.findByDateBetween(any(), any())).thenReturn(transactions);
        when(budgetRepository.findByCategory("groceries"))
                .thenReturn(Optional.of(new Budget("groceries", new BigDecimal("400.00"))));

        MonthlySummary summary = summaryService.getMonthlySummary(2026, 3);

        CategorySummary groceries = summary.getCategories().get(0);
        assertThat(groceries.getBudgetLimit()).isEqualByComparingTo("400.00");
        assertThat(groceries.isOverBudget()).isTrue();
    }

    @Test
    void doesNotFlagOverBudgetWhenExpenseStaysUnderTheLimit() {
        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("100.00"), "groceries", TransactionType.EXPENSE, 5)
        );
        when(transactionRepository.findByDateBetween(any(), any())).thenReturn(transactions);
        when(budgetRepository.findByCategory("groceries"))
                .thenReturn(Optional.of(new Budget("groceries", new BigDecimal("400.00"))));

        MonthlySummary summary = summaryService.getMonthlySummary(2026, 3);

        assertThat(summary.getCategories().get(0).isOverBudget()).isFalse();
    }

    @Test
    void categoryWithNoBudgetSetIsNeverOverBudget() {
        List<Transaction> transactions = List.of(
                transaction(new BigDecimal("5000.00"), "misc", TransactionType.EXPENSE, 5)
        );
        when(transactionRepository.findByDateBetween(any(), any())).thenReturn(transactions);
        when(budgetRepository.findByCategory("misc")).thenReturn(Optional.empty());

        MonthlySummary summary = summaryService.getMonthlySummary(2026, 3);

        CategorySummary misc = summary.getCategories().get(0);
        assertThat(misc.getBudgetLimit()).isNull();
        assertThat(misc.isOverBudget()).isFalse();
    }

    @Test
    void emptyMonthProducesZeroTotalsAndNoCategories() {
        when(transactionRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        MonthlySummary summary = summaryService.getMonthlySummary(2026, 4);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getCategories()).isEmpty();
    }
}
