package com.jihedapps.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class CreateAccountRequest {

    @NotBlank(message = "Le nom du titulaire est obligatoire")
    private String ownerName;

    @NotNull(message = "Le solde initial est obligatoire")
    @PositiveOrZero(message = "Le solde initial doit être supérieur ou égal à zéro")
    private BigDecimal initialBalance;

    @PositiveOrZero(message = "Le découvert autorisé doit être supérieur ou égal à zéro")
    private BigDecimal overdraftLimit;

    public CreateAccountRequest() {}

    public CreateAccountRequest(String ownerName, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        this.ownerName = ownerName;
        this.initialBalance = initialBalance;
        this.overdraftLimit = overdraftLimit;
    }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }
}
