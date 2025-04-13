package com.awesome.gic.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDate date;
    private String transactionId;
    private String type;
    private double amount;

    public Transaction(LocalDate date, String transactionId, String type, double amount) {
        this.date = date;
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
    }

    public LocalDate getDate() {
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

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}