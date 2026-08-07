package com.jihedapps.banking.service;

import com.jihedapps.banking.entity.Transaction;
import com.jihedapps.banking.entity.TransactionStatus;
import com.jihedapps.banking.entity.TransactionType;
import com.jihedapps.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionAuditService {

    private final TransactionRepository transactionRepository;

    public TransactionAuditService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction logTransaction(String source, String target, BigDecimal amount,
                                       TransactionType type, TransactionStatus status, String failureReason) {
        return transactionRepository.save(new Transaction(source, target, amount, type, status, failureReason));
    }
}
