package com.minibank.service;

import com.minibank.entity.Account;
import com.minibank.entity.Client;

import java.math.BigDecimal;

public class BankService {
    private final ClientService clientService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public BankService(
            ClientService clientService,
            AccountService accountService,
            TransactionService transactionService
    ) {
        this.clientService = clientService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public Client registerClient(String name, String email) {
        return clientService.registerClient(name, email);
    }

    public Account openAccount(Long clientId, String accountNumber) {
        return accountService.openAccount(clientId, accountNumber);
    }

    public Account openAccount(Long clientId, String accountNumber, BigDecimal balance) {
        return accountService.openAccount(clientId, accountNumber, balance);
    }

    public Account getAccount(Long accountId) {
        return accountService.getAccount(accountId);
    }

    public Client getClient(Long clientId) {
        return clientService.getClient(clientId);
    }

    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account from = getAccount(fromAccountId);
        Account to = getAccount(toAccountId);

        checkTransfer(from, amount);

        BigDecimal newFromBalance = from.getBalance().subtract(amount);
        accountService.updateBalance(fromAccountId, newFromBalance);
        recordTransaction(fromAccountId, amount.negate(), "TRANSFER_OUT");

        BigDecimal newToBalance = to.getBalance().add(amount);
        accountService.updateBalance(toAccountId, newToBalance);
        recordTransaction(toAccountId, amount, "TRANSFER_IN");
    }

    private static void checkTransfer(Account from, BigDecimal amount) {
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds on account " + from.getId());
        }
    }

    private void recordTransaction(Long accountId, BigDecimal amount, String type) {
        transactionService.recordTransaction(accountId, amount, type);
    }
}
