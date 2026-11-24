package com.minibank.factory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

public class EntityManagerFactory {

    private static final jakarta.persistence.EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("mini_bank");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
