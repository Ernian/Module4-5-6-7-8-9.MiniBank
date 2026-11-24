package com.minibank.service;

import com.minibank.entity.Account;
import com.minibank.entity.Client;
import com.minibank.entity.Transaction;
import com.minibank.factory.EntityManagerFactory;
import com.minibank.server.dto.ClientDto;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.util.List;

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

    public boolean transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            var entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();

            try {
                Account from = entityManager.find(Account.class, fromAccountId);
                Account to = entityManager.find(Account.class, toAccountId);

                checkTransfer(from, amount);

                from.setBalance(from.getBalance().subtract(amount));
                entityManager.persist(new Transaction(
                        from, amount.negate(), "TRANSFER_OUT"));

                to.setBalance(to.getBalance().add(amount));
                entityManager.persist(new Transaction(
                        to, amount, "TRANSFER_IN"));

                entityTransaction.commit();
                return true;
            } catch (Exception e) {
                entityTransaction.rollback();
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private static void checkTransfer(Account from, BigDecimal amount) {
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds on account " + from.getId());
        }
    }

    private boolean recordTransaction(Account account, BigDecimal amount, String type) {
        return transactionService.recordTransaction(account.getId(), amount, type);
    }

    public List<ClientDto> getAllClientsDto() {
        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            EntityTransaction entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            List<Client> clients = entityManager
                    .createQuery("SELECT c FROM Client c JOIN FETCH c.accounts", Client.class)
                    .getResultList();

            List<ClientDto> dtoList = clients.stream()
                    .map(ClientDto::fromEntity)
                    .toList();

            entityTransaction.commit();
            return dtoList;
        }
    }
}
