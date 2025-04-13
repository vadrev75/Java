package com.awesome.gic.models;

public class Statement {
    private String date;
    private String transactionId;
    private String type;
    private double amount;
    private double balance;

    public Statement(String date, String transactionId, String type, double amount, double balance) {
        this.date = date;
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.balance = balance;
    }

    public String getDate() {
        return date;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalance() {
        return balance;
    }
}
