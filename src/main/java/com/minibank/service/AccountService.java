package com.minibank.service;

import com.minibank.entity.Account;
import com.minibank.entity.Client;
import com.minibank.factory.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;

public class AccountService {

    public Account openAccount(Long clientId, String accountNumber, BigDecimal balance) {
        EntityTransaction entityTransaction = null;

        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();

            Client client = entityManager.find(Client.class, clientId);
            Account account = new Account(client, accountNumber, balance);
            entityManager.persist(account);
            entityTransaction.commit();

            return account;
        } catch (Exception e) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Account openAccount(Long clientId, String accountNumber) {
        return openAccount(clientId, accountNumber, new BigDecimal(0));
    }

    public Account getAccount(Long accountId) {
        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            return entityManager.find(Account.class, accountId);
        }
    }

    public void updateBalance(Long accountId, BigDecimal balance) {
        EntityTransaction entityTransaction = null;

        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            Account account = entityManager.find(Account.class, accountId);
            account.setBalance(balance);
            entityTransaction.commit();
        } catch (Exception e) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
