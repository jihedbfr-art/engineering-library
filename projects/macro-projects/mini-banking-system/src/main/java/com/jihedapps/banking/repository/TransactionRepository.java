package com.jihedapps.banking.repository;

import com.jihedapps.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccountNumber = :accNum OR t.targetAccountNumber = :accNum ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountNumber(String accNum);
}
