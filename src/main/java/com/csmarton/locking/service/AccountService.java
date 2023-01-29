package com.csmarton.locking.service;

import com.csmarton.locking.Account;
import com.csmarton.locking.repo.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class AccountService {
    private static SessionFactory sessionFactory;


    public AccountService() {
        sessionFactory = HibernateUtil.getSessionFactory();
        System.out.println("SessionFactory created");
    }

    public void persistAccount(String iban, double balance) {
        Session session = sessionFactory.openSession();
        Account account1 = new Account(iban, balance);
        session.getTransaction().begin();
        session.persist(account1);
        session.getTransaction().commit();
        session.close();
        log("Account persisted", account1);
    }

    public Account getAccount(String iban) {
        Session session = sessionFactory.openSession();

        try {//some delay before reading
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log("before acquiring read lock");
        session.getTransaction().begin();

        TypedQuery<Account> query = session.createQuery("from Account a where a.iban=:iban", Account.class);
        query.setParameter("iban", iban);

        query.setLockMode(LockModeType.PESSIMISTIC_READ);
        Account account = query.getSingleResult();

        log("After acquiring read lock", account);
        session.getTransaction().commit();
        session.close();
        log("Account after read commit", account);

        return account;
    }
    public void initiateTransfer(String fromIban, String toIban, double amount) {
        Session session = sessionFactory.openSession();

        session.getTransaction().begin();

        TypedQuery<Account> query = session.createQuery("from Account a where a.iban=:iban", Account.class);
        query.setParameter("iban", fromIban);

        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        Account fromAccount = query.getSingleResult();

        query.setParameter("iban", toIban);
        Account toAccount = query.getSingleResult();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fromAccount.changeBalanceWith(-1 * amount);
        toAccount.changeBalanceWith(amount);

        log("committing in write thread.");
        session.getTransaction().commit();
        session.close();
        log("FromAccount updated", fromAccount);
        log("ToAccount updated", toAccount);
    }

    private static void log(Object... msgs) {
        System.out.println(LocalTime.now() + " - " + Thread.currentThread().getName() +
                " - " + Arrays.toString(msgs));
    }

    public static void main(String[] args) {
        AccountService accountService = new AccountService();

        accountService.persistAccount("HU123456", 100);
        accountService.persistAccount("DE456789", 100);

        System.out.println("End");
    }
}
