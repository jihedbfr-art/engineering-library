package com.jihedapps.banking.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("Compte introuvable : " + accountNumber);
    }
}
