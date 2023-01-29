package com.csmarton.locking;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PessimisticLockExample {
    private static EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("account-unit");

    public static void main(String[] args) throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(3);
        try {
            persistAccount();
            es.execute(() -> {
                updateAccount();
            });
            es.execute(() -> {
                //simulating other user by using different thread
                readAccount();
            });
            es.shutdown();
            es.awaitTermination(1, TimeUnit.MINUTES);
        } finally {
            entityManagerFactory.close();
        }
    }

    private static void updateAccount() {
        log("updating Account entity");
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Account account = em.find(Account.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        account.changeBalanceWith(20);
        log("committing in write thread.");
        em.getTransaction().commit();
        em.close();
        log("Account updated", account);
    }

    private static void readAccount() {
        try {//some delay before reading
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log("before acquiring read lock");
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Account account = em.find(Account.class, 1L, LockModeType.PESSIMISTIC_READ);
        log("After acquiring read lock", account);
        em.getTransaction().commit();
        em.close();
        log("Article after read commit", account);
    }

    public static void persistAccount() {
        log("persisting account");
        Account account1 = new Account("HU123456", 100);
        Account account2 = new Account("DE456789", 100);
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(account1);
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.persist(account2);
        em.getTransaction().commit();
        em.close();
        log("First account persisted", account1);
        log("Second account persisted", account2);
    }

    private static void log(Object... msgs) {
        System.out.println(LocalTime.now() + " - " + Thread.currentThread().getName() +
                " - " + Arrays.toString(msgs));
    }
}