package com.minibank.service;

import com.minibank.entity.Account;
import com.minibank.entity.Transaction;
import com.minibank.factory.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;


import java.math.BigDecimal;

public class TransactionService {
    public boolean recordTransaction(Long accountId, BigDecimal amount, String type) {
        EntityTransaction entityTransaction = null;

        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();

            Account account = entityManager.find(Account.class, accountId);
            var tr = new Transaction(account, amount, type);
            entityManager.persist(tr);
            entityTransaction.commit();

            return true;
        } catch (Exception e) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
            }

            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
