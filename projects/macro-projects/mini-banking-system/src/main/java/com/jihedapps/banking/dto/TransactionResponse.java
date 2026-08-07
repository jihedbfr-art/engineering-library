package com.jihedapps.banking.dto;

import com.jihedapps.banking.entity.Transaction;
import com.jihedapps.banking.entity.TransactionStatus;
import com.jihedapps.banking.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String failureReason;
    private LocalDateTime timestamp;

    public TransactionResponse() {}

    public static TransactionResponse from(Transaction tx) {
        TransactionResponse res = new TransactionResponse();
        res.id = tx.getId();
        res.sourceAccountNumber = tx.getSourceAccountNumber();
        res.targetAccountNumber = tx.getTargetAccountNumber();
        res.amount = tx.getAmount();
        res.type = tx.getType();
        res.status = tx.getStatus();
        res.failureReason = tx.getFailureReason();
        res.timestamp = tx.getTimestamp();
        return res;
    }

    public Long getId() { return id; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public String getTargetAccountNumber() { return targetAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
