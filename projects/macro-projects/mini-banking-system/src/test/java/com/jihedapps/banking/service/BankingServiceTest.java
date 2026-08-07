package com.jihedapps.banking.service;

import com.jihedapps.banking.dto.*;
import com.jihedapps.banking.entity.TransactionStatus;
import com.jihedapps.banking.entity.TransactionType;
import com.jihedapps.banking.exception.InsufficientBalanceException;
import com.jihedapps.banking.exception.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BankingServiceTest {

    @Autowired
    private BankingService bankingService;

    private AccountResponse acc1;
    private AccountResponse acc2;

    @BeforeEach
    void setUp() {
        acc1 = bankingService.createAccount(new CreateAccountRequest("Alice", new BigDecimal("1000.00"), new BigDecimal("200.00")));
        acc2 = bankingService.createAccount(new CreateAccountRequest("Bob", new BigDecimal("500.00"), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Virement réussi entre deux comptes")
    void testSuccessfulTransfer() {
        TransferRequest req = new TransferRequest(acc1.getAccountNumber(), acc2.getAccountNumber(), new BigDecimal("300.00"));
        TransactionResponse tx = bankingService.transfer(req);

        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
        assertEquals(TransactionType.TRANSFER, tx.getType());

        AccountResponse updated1 = bankingService.getAccount(acc1.getAccountNumber());
        AccountResponse updated2 = bankingService.getAccount(acc2.getAccountNumber());

        assertEquals(new BigDecimal("700.0000"), updated1.getBalance());
        assertEquals(new BigDecimal("800.0000"), updated2.getBalance());
    }

    @Test
    @DisplayName("Virement utilisant le découvert autorisé")
    void testTransferUsingOverdraft() {
        // Acc1 a 1000 + 200 decouvert = 1200 max. On vire 1100.
        TransferRequest req = new TransferRequest(acc1.getAccountNumber(), acc2.getAccountNumber(), new BigDecimal("1100.00"));
        TransactionResponse tx = bankingService.transfer(req);

        assertEquals(TransactionStatus.SUCCESS, tx.getStatus());

        AccountResponse updated1 = bankingService.getAccount(acc1.getAccountNumber());
        assertEquals(new BigDecimal("-100.0000"), updated1.getBalance());
    }

    @Test
    @DisplayName("Échec de virement pour solde insuffisant et vérification du log d'audit FAILED")
    void testTransferInsufficientBalance() {
        // Acc1 a 1000 + 200 = 1200 max. On essaye de virer 1500.
        TransferRequest req = new TransferRequest(acc1.getAccountNumber(), acc2.getAccountNumber(), new BigDecimal("1500.00"));

        assertThrows(InsufficientBalanceException.class, () -> bankingService.transfer(req));

        // Verifier que les soldes n'ont PAS bougé (Atomicités)
        AccountResponse updated1 = bankingService.getAccount(acc1.getAccountNumber());
        AccountResponse updated2 = bankingService.getAccount(acc2.getAccountNumber());
        assertEquals(new BigDecimal("1000.0000"), updated1.getBalance());
        assertEquals(new BigDecimal("500.0000"), updated2.getBalance());

        // Verifier la présence de la transaction échouée dans l'audit log
        List<TransactionResponse> history = bankingService.getAccountHistory(acc1.getAccountNumber());
        assertTrue(history.stream().anyMatch(t -> t.getStatus() == TransactionStatus.FAILED));
    }

    @Test
    @DisplayName("Interdiction de virement sur le même compte")
    void testTransferSameAccount() {
        TransferRequest req = new TransferRequest(acc1.getAccountNumber(), acc1.getAccountNumber(), new BigDecimal("100.00"));
        assertThrows(InvalidTransactionException.class, () -> bankingService.transfer(req));
    }
}
