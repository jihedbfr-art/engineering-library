package com.jihedapps.financedashboard.service;

import com.jihedapps.financedashboard.dto.CategorySummary;
import com.jihedapps.financedashboard.dto.MonthlySummary;
import com.jihedapps.financedashboard.entity.Budget;
import com.jihedapps.financedashboard.entity.Transaction;
import com.jihedapps.financedashboard.entity.TransactionType;
import com.jihedapps.financedashboard.repository.BudgetRepository;
import com.jihedapps.financedashboard.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public SummaryService(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public MonthlySummary getMonthlySummary(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findByDateBetween(start, end);

        Map<String, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory));

        List<CategorySummary> categorySummaries = byCategory.entrySet().stream()
                .map(entry -> buildCategorySummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);

        return new MonthlySummary(year, month, totalIncome, totalExpense,
                totalIncome.subtract(totalExpense), categorySummaries);
    }

    private CategorySummary buildCategorySummary(String category, List<Transaction> transactions) {
        BigDecimal income = sumByType(transactions, TransactionType.INCOME);
        BigDecimal expense = sumByType(transactions, TransactionType.EXPENSE);

        BigDecimal limit = budgetRepository.findByCategory(category)
                .map(Budget::getMonthlyLimit)
                .orElse(null);

        boolean overBudget = limit != null && expense.compareTo(limit) > 0;

        return new CategorySummary(category, income, expense, limit, overBudget);
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
