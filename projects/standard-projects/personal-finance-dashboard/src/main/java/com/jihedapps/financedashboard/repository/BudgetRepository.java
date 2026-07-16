package com.jihedapps.financedashboard.repository;

import com.jihedapps.financedashboard.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByCategory(String category);
}
