package com.jihedapps.banking.service;

import com.jihedapps.banking.dto.*;
import com.jihedapps.banking.entity.*;
import com.jihedapps.banking.exception.*;
import com.jihedapps.banking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    public BankingService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          TransactionAuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest req) {
        String accountNumber = "ACCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal balance = req.getInitialBalance() != null ? req.getInitialBalance() : BigDecimal.ZERO;
        BigDecimal overdraft = req.getOverdraftLimit() != null ? req.getOverdraftLimit() : BigDecimal.ZERO;

        Account account = new Account(accountNumber, req.getOwnerName(), balance, overdraft);
        Account saved = accountRepository.save(account);

        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            auditService.logTransaction(null, accountNumber, balance,
                    TransactionType.DEPOSIT, TransactionStatus.SUCCESS, "Initial Deposit");
        }

        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber) {
        Account account = findAccountOrThrow(accountNumber);
        return AccountResponse.from(account);
    }

    @Transactional
    public TransactionResponse deposit(TransactionRequest req) {
        Account account = findAccountOrThrow(req.getAccountNumber());
        account.setBalance(account.getBalance().add(req.getAmount()));
        accountRepository.save(account);

        Transaction tx = auditService.logTransaction(null, req.getAccountNumber(), req.getAmount(),
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS, null);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse withdraw(TransactionRequest req) {
        Account account = findAccountOrThrow(req.getAccountNumber());

        if (!account.canWithdraw(req.getAmount())) {
            BigDecimal available = account.getBalance().add(account.getOverdraftLimit());
            auditService.logTransaction(req.getAccountNumber(), null, req.getAmount(),
                    TransactionType.WITHDRAWAL, TransactionStatus.FAILED, "Solde insuffisant");
            throw new InsufficientBalanceException(req.getAccountNumber(), req.getAmount(), available);
        }

        account.setBalance(account.getBalance().subtract(req.getAmount()));
        accountRepository.save(account);

        Transaction tx = auditService.logTransaction(req.getAccountNumber(), null, req.getAmount(),
                TransactionType.WITHDRAWAL, TransactionStatus.SUCCESS, null);
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest req) {
        if (req.getSourceAccountNumber().equals(req.getTargetAccountNumber())) {
            throw new InvalidTransactionException("Impossible de virer vers le même compte");
        }

        Account source = findAccountOrThrow(req.getSourceAccountNumber());
        Account target = findAccountOrThrow(req.getTargetAccountNumber());

        if (!source.canWithdraw(req.getAmount())) {
            BigDecimal available = source.getBalance().add(source.getOverdraftLimit());
            auditService.logTransaction(req.getSourceAccountNumber(), req.getTargetAccountNumber(),
                    req.getAmount(), TransactionType.TRANSFER, TransactionStatus.FAILED, "Solde source insuffisant");
            throw new InsufficientBalanceException(req.getSourceAccountNumber(), req.getAmount(), available);
        }

        source.setBalance(source.getBalance().subtract(req.getAmount()));
        target.setBalance(target.getBalance().add(req.getAmount()));

        accountRepository.save(source);
        accountRepository.save(target);

        Transaction tx = auditService.logTransaction(
                req.getSourceAccountNumber(), req.getTargetAccountNumber(), req.getAmount(),
                TransactionType.TRANSFER, TransactionStatus.SUCCESS, null);
        return TransactionResponse.from(tx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountHistory(String accountNumber) {
        findAccountOrThrow(accountNumber);
        return transactionRepository.findByAccountNumber(accountNumber)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    private Account findAccountOrThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}
