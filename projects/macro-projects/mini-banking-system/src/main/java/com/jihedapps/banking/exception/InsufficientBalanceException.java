package com.jihedapps.banking.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String accountNumber, BigDecimal requested, BigDecimal available) {
        super(String.format("Solde insuffisant pour le compte %s. Demandé: %s, Disponible (avec découvert): %s",
                accountNumber, requested, available));
    }
}
