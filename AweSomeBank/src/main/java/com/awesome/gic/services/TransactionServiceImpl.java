package com.awesome.gic.services;

import com.awesome.gic.interfaces.AccountService;
import com.awesome.gic.interfaces.InterestRuleService;
import com.awesome.gic.interfaces.TransactionService;
import com.awesome.gic.models.Account;
import com.awesome.gic.models.InterestRule;
import com.awesome.gic.models.Statement;
import com.awesome.gic.models.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TransactionServiceImpl implements TransactionService {
    private AccountService accountService;
    private InterestRuleService interestRuleService;
    private Map<LocalDate, Integer> transactionCounts;
    private DateTimeFormatter dateFormatter;

    public TransactionServiceImpl(AccountService accountService, InterestRuleService interestRuleService) {
        this.accountService = accountService;
        this.interestRuleService = interestRuleService;
        this.transactionCounts = new HashMap<>();
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    }

    @Override
    public Transaction createTransaction(String dateStr, String accountId, String type, double amount) throws Exception {
        // Validate date format
        if (!dateStr.matches("\\d{8}")) {
            throw new Exception("Date should be in YYYYMMdd format");
        }

        LocalDate date = parseDate(dateStr);

        // Validate type
        if (!type.equalsIgnoreCase("D") && !type.equalsIgnoreCase("W")) {
            throw new Exception("Transaction type should be D for deposit or W for withdrawal");
        }

        // Validate amount
        if (amount <= 0) {
            throw new Exception("Amount must be greater than zero");
        }

        // Check if amount has more than 2 decimal places
        BigDecimal bdAmount = new BigDecimal(amount);
        bdAmount = bdAmount.setScale(2, RoundingMode.HALF_UP);
        if (bdAmount.doubleValue() != amount) {
            throw new Exception("Amount can have at most 2 decimal places");
        }

        // Check if account exists, if not create a new one
        Account account = accountService.getAccount(accountId);
        if (account == null) {
            account = accountService.createAccount(accountId);
        }

        // Validate withdrawal
        if (type.equalsIgnoreCase("W") && (account.getBalance() < amount)) {
            throw new Exception("Insufficient balance for withdrawal");
        }

        // Generate transaction ID
        String transactionId = generateTransactionId(date);

        // Create and add transaction
        Transaction transaction = new Transaction(date, transactionId, type.toUpperCase(), amount);
        account.addTransaction(transaction);

        return transaction;
    }

    public String generateTransactionId(LocalDate date) {
        int count = getTransactionCount(date) + 1;
        transactionCounts.put(date, count);
        return date.format(dateFormatter) + "-" + String.format("%02d", count);
    }

    @Override
    public int getTransactionCount(LocalDate date) {
        return transactionCounts.getOrDefault(date, 0);
    }

    @Override
    public List<Statement> generateMonthlyStatement(String accountId, String yearMonth) {
        List<Statement> statement = new ArrayList<>();
        Account account = accountService.getAccount(accountId);

        if (account == null) {
            return statement;
        }

        //Check if yearmonth matches format YYYYMM
        if (!yearMonth.matches("\\d{6}")) {
            return statement; // Invalid year-month format
        }

        int year = Integer.parseInt(yearMonth.substring(0, 4));
        int month = Integer.parseInt(yearMonth.substring(4, 6));

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        // Get all transactions for the month
        List<Transaction> monthTransactions = new ArrayList<>();
        for (Transaction transaction : account.getTransactions()) {
            //System.out.println(transaction.getDate().isBefore(startOfMonth) && !transaction.getDate().isAfter(endOfMonth));
            if (!transaction.getDate().isBefore(startOfMonth) && !transaction.getDate().isAfter(endOfMonth)) {
                monthTransactions.add(transaction);
            }
        }

        // Sort transactions by date
        monthTransactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

        // Calculate running balance and create statement lines
        //System.out.println("starting balance: " + getStartingBalance(account, startOfMonth));
        double runningBalance = getStartingBalance(account, startOfMonth);

        for (Transaction transaction : monthTransactions) {
            if (transaction.getType().equalsIgnoreCase("D")) {
                runningBalance += transaction.getAmount();
            } else if (transaction.getType().equalsIgnoreCase("W")) {
                runningBalance -= transaction.getAmount();
            } else if (transaction.getType().equalsIgnoreCase("I")) {
                runningBalance += transaction.getAmount();
            }

            //System.out.println("running balance: " + runningBalance);

            statement.add(new Statement(
                    transaction.getFormattedDate(),
                    transaction.getTransactionId(),
                    transaction.getType(),
                    transaction.getAmount(),
                    runningBalance
            ));

        }

        // Calculate and add interest
        double interest = getMonthlyInterest(account, startOfMonth, endOfMonth);
        //System.out.println("monthly interest: " + interest);
        if (interest > 0) {
            runningBalance += interest;

            statement.add(new Statement(
                    endOfMonth.format(dateFormatter),
                    null,
                    "I",
                    interest,
                    runningBalance
            ));

            //System.out.println("running balance2: " + runningBalance);
        }

        return statement;
    }

    public double getStartingBalance(Account account, LocalDate startOfMonth) {
        double balance = 0.0;

        for (Transaction transaction : account.getTransactions()) {
            if (transaction.getDate().isBefore(startOfMonth)) {
                if (transaction.getType().equalsIgnoreCase("D") || transaction.getType().equalsIgnoreCase("I")) {
                    balance += transaction.getAmount();
                } else if (transaction.getType().equalsIgnoreCase("W")) {
                    balance -= transaction.getAmount();
                }
            }
        }

        return balance;
    }

    public double getMonthlyInterest(Account account, LocalDate startOfMonth, LocalDate endOfMonth) {
        // Get all transactions for the account sorted by date
        List<Transaction> sortedTransactions = new ArrayList<>(account.getTransactions());
        sortedTransactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

        // Filter transactions that occur before or during the month
        List<Transaction> relevantTransactions = new ArrayList<>();
        for (Transaction t : sortedTransactions) {
            if (!t.getDate().isAfter(endOfMonth)) {
                relevantTransactions.add(t);
            }
        }

        // initial balance at the start of the month
        double initialBalance = getStartingBalance(account, startOfMonth);

        // Create list of dates when balance changes (either transaction or rule change)
        List<LocalDate> txnDates = new ArrayList<>();

        // Add transaction dates
        for (Transaction t : relevantTransactions) {
            if (!t.getDate().isBefore(startOfMonth) && !t.getDate().isAfter(endOfMonth)) {
                if (!txnDates.contains(t.getDate())) {
                    txnDates.add(t.getDate());
                }
            }
        }

        // Add interest rule change dates
        List<InterestRule> allRules = interestRuleService.getAllInterestRules();
        for (InterestRule rule : allRules) {
            if (!rule.getDate().isBefore(startOfMonth) && !rule.getDate().isAfter(endOfMonth)) {
                if (!txnDates.contains(rule.getDate())) {
                    txnDates.add(rule.getDate());
                }
            }
        }

        // Add start and end dates if not already included
        if (!txnDates.contains(startOfMonth)) {
            txnDates.add(startOfMonth);
        }
        if (!txnDates.contains(endOfMonth.plusDays(1))) {
            txnDates.add(endOfMonth.plusDays(1));
        }

        // Sort all dates
        Collections.sort(txnDates);

        // Calculate interest
        double totalInterest = 0.0;
        double currentBalance = initialBalance;
        LocalDate currentDate = startOfMonth;
        InterestRule currentRule = interestRuleService.getApplicableInterestRule(currentDate.minusDays(1));

        for (int i = 0; i < txnDates.size(); i++) {
            LocalDate nextDate = txnDates.get(i);

            // Skip if the date is before our current position
            if (nextDate.isBefore(currentDate)) {
                continue;
            }

            // Calculate interest for the period from currentDate to nextDate
            if (currentRule != null && !currentDate.isAfter(endOfMonth)) {
                long days = findDays(currentDate, nextDate.isAfter(endOfMonth) ? endOfMonth.plusDays(1) : nextDate);
                if (days > 0) {
                    double periodInterest = (currentBalance * currentRule.getRate() / 100.0 * days) / 365.0;
                    totalInterest += periodInterest;
                }
            }

            // Update current date
            currentDate = nextDate;

            // Update balance with any transactions on this date
            for (Transaction transaction : relevantTransactions) {
                if (transaction.getDate().equals(currentDate) && !transaction.getDate().isAfter(endOfMonth)) {
                    if (transaction.getType().equalsIgnoreCase("D") || transaction.getType().equalsIgnoreCase("I")) {
                        currentBalance += transaction.getAmount();
                    } else if (transaction.getType().equalsIgnoreCase("W")) {
                        currentBalance -= transaction.getAmount();
                    }
                }
            }

            // Update interest rule if there's a change on this date
            boolean ruleChanged = false;
            for (InterestRule rule : allRules) {
                if (rule.getDate().equals(currentDate)) {
                    currentRule = rule;
                    ruleChanged = true;
                    break;
                }
            }

            // If no specific rule change on this date but we need the applicable rule
            if (!ruleChanged) {
                currentRule = interestRuleService.getApplicableInterestRule(currentDate);
            }
        }

        // Round to 2 decimal places
        BigDecimal bd = new BigDecimal(totalInterest);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public long findDays(LocalDate currentDate, LocalDate endDate) {
        if (currentDate.isAfter(endDate)) {
            return 0;
        }

        return endDate.toEpochDay() - currentDate.toEpochDay();
    }

    public LocalDate parseDate(String dateStr) throws Exception {
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (Exception e) {
            throw new Exception("Invalid date format. Please use YYYYMMdd");
        }
    }
}
