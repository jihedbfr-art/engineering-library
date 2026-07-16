package com.jihedapps.financedashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class MonthlySummary {

    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private List<CategorySummary> categories;

    public MonthlySummary(int year, int month, BigDecimal totalIncome, BigDecimal totalExpense,
                           BigDecimal balance, List<CategorySummary> categories) {
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.balance = balance;
        this.categories = categories;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<CategorySummary> getCategories() {
        return categories;
    }
}
