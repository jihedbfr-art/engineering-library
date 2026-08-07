package com.jihedapps.banking.dto;

import com.jihedapps.banking.entity.Account;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {
    private String accountNumber;
    private String ownerName;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;
    private LocalDateTime createdAt;

    public AccountResponse() {}

    public static AccountResponse from(Account account) {
        AccountResponse res = new AccountResponse();
        res.accountNumber = account.getAccountNumber();
        res.ownerName = account.getOwnerName();
        res.balance = account.getBalance();
        res.overdraftLimit = account.getOverdraftLimit();
        res.createdAt = account.getCreatedAt();
        return res;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
