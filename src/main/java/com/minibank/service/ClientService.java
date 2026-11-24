package com.minibank.service;

import com.minibank.entity.Client;
import com.minibank.factory.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class ClientService {
    public Client registerClient(String name, String email) {
        EntityTransaction entityTransaction = null;

        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();

            Client client = new Client(name, email);
            entityManager.persist(client);
            entityTransaction.commit();

            return client;
        } catch (Exception e) {
            if (entityTransaction != null) {
                entityTransaction.rollback();
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Client getClient(Long clientId) {
        try (var entityManager = EntityManagerFactory.getEntityManager()) {
            return entityManager.find(Client.class, clientId);
        }
    }
}
