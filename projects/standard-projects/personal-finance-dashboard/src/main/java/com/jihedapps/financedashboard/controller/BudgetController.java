package com.jihedapps.financedashboard.controller;

import com.jihedapps.financedashboard.entity.Budget;
import com.jihedapps.financedashboard.repository.BudgetRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;

    public BudgetController(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @GetMapping
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Budget create(@Valid @RequestBody Budget budget) {
        budget.setId(null);
        return budgetRepository.save(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> update(@PathVariable Long id, @Valid @RequestBody Budget update) {
        return budgetRepository.findById(id)
                .map(existing -> {
                    existing.setCategory(update.getCategory());
                    existing.setMonthlyLimit(update.getMonthlyLimit());
                    return ResponseEntity.ok(budgetRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!budgetRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        budgetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
