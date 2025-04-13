package com.awesome.gic.interfaces;

import com.awesome.gic.models.Statement;
import com.awesome.gic.models.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    Transaction createTransaction(String date, String accountId, String type, double amount) throws Exception;
    List<Statement> generateMonthlyStatement(String accountId, String yearMonth);
    int getTransactionCount(LocalDate date);
}