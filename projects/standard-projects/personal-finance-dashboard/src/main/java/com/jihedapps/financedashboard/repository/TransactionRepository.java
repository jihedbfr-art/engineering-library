package com.jihedapps.financedashboard.repository;

import com.jihedapps.financedashboard.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

    List<Transaction> findByCategoryAndDateBetween(String category, LocalDate start, LocalDate end);
}
