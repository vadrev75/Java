package com.awesome.gic.models;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private String accountId;
    private double balance;
    private List<Transaction> transactions;

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public String getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);

        if (transaction.getType().equalsIgnoreCase("D")) {
            balance += transaction.getAmount();
        } else if (transaction.getType().equalsIgnoreCase("W")) {
            balance -= transaction.getAmount();
        } else if (transaction.getType().equalsIgnoreCase("I")) {
            balance += transaction.getAmount();
        }
    }
}
