package com.jihedapps.financedashboard.dto;

import java.math.BigDecimal;

public class CategorySummary {

    private String category;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal budgetLimit;
    private boolean overBudget;

    public CategorySummary(String category, BigDecimal totalIncome, BigDecimal totalExpense,
                            BigDecimal budgetLimit, boolean overBudget) {
        this.category = category;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.budgetLimit = budgetLimit;
        this.overBudget = overBudget;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public boolean isOverBudget() {
        return overBudget;
    }
}
