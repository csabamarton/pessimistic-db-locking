package com.csmarton.locking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Account {
    @Id
    @GeneratedValue
    private long id;

    private String iban;
    private double balance;

    public Account() {
    }

    public Account(String iban, double balance) {
        this.iban = iban;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", iban='" + iban + '\'' +
                ", balance=" + balance +
                '}';
    }

    public void changeBalanceWith(double change) {
        balance += change;
    }
}