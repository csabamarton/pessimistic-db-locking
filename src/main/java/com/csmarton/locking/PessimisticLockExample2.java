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

public class PessimisticLockExample2 {
    private static EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("account-unit");

    public static void main(String[] args) throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(3);
        try {
            persistAccount();
            es.execute(() -> {
                //simulating other user read by using different thread
                readAccount();
            });
            es.execute(() -> {
                updateAccount();
            });

            es.shutdown();
            es.awaitTermination(1, TimeUnit.MINUTES);
        } finally {
            entityManagerFactory.close();
        }
    }

    private static void updateAccount() {
        try {//some delay before writing
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        log("write thread before acquiring lock");
        Account account = em.find(Account.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        log("write thread after acquiring lock");
        account.changeBalanceWith(20);
        log("committing in write thread.");
        em.getTransaction().commit();
        em.close();
        log("Article updated", account);
    }

    private static void readAccount() {
        log("before acquiring read lock");
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        Account account = em.find(Account.class, 1L, LockModeType.PESSIMISTIC_READ);
        log("After acquiring read lock", account);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        em.getTransaction().commit();
        em.close();
        log("Account after read commit", account);
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