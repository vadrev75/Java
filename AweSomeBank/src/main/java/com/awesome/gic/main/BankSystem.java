package com.awesome.gic.main;

import com.awesome.gic.interfaces.AccountService;
import com.awesome.gic.interfaces.InterestRuleService;
import com.awesome.gic.interfaces.TransactionService;
import com.awesome.gic.models.Account;
import com.awesome.gic.models.InterestRule;
import com.awesome.gic.models.Statement;
import com.awesome.gic.models.Transaction;
import com.awesome.gic.services.AccountServiceImpl;
import com.awesome.gic.services.InterestRuleServiceImpl;
import com.awesome.gic.services.TransactionServiceImpl;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class BankSystem {
    private Scanner scanner;
    private AccountService accountService;
    private InterestRuleService interestRuleService;
    private TransactionService transactionService;
    private DateTimeFormatter dateFormatter;

    public BankSystem() {
        scanner = new Scanner(System.in);
        accountService = new AccountServiceImpl();
        interestRuleService = new InterestRuleServiceImpl();
        transactionService = new TransactionServiceImpl(accountService, interestRuleService);
        dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    }

    public void run() {
        boolean running = true;

        System.out.println("Welcome to AwesomeGIC Bank!");

        while (running) {
            displayMainMenu();
            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.isEmpty() || choice.length() == 0) {
                continue;
            }

            char option = choice.charAt(0);

            switch (option) {
                case 'T':
                    inputTransaction();
                    break;
                case 'I':
                    inputInterestRule();
                    break;
                case 'P':
                    printStatement();
                    break;
                case 'Q':
                    running = false;
                    System.out.println("Thank you for banking with AwesomeGIC Bank.");
                    System.out.println("Have a nice day!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
    }

    private void displayMainMenu() {
        System.out.println("What would you like to do?");
        System.out.println("[T] Input transactions");
        System.out.println("[I] Define interest rules");
        System.out.println("[P] Print statement");
        System.out.println("[Q] Quit");
        System.out.print("> ");
    }

    private void inputTransaction() {
        System.out.println("Please enter transaction details in <Date> <Account> <Type> <Amount> format");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid input format");
            }

            String date = parts[0];
            String accountId = parts[1];
            String type = parts[2];
            double amount = Double.parseDouble(parts[3]);

            Transaction transaction = transactionService.createTransaction(date, accountId, type, amount);

            if (transaction != null) {
                // Print account statement after transaction
                Account account = accountService.getAccount(accountId);
                System.out.println("Account: " + accountId);
                System.out.println("| Date     | Txn Id      | Type | Amount |");

                for (Transaction t : account.getTransactions()) {
                    System.out.printf("| %s | %s | %-4s | %6.2f |\n",
                            t.getFormattedDate(), t.getTransactionId(), t.getType(), t.getAmount());
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void inputInterestRule() {
        System.out.println("Please enter interest rules details in <Date> <RuleId> <Rate in %> format");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid input format");
            }

            String date = parts[0];
            String ruleId = parts[1];
            double rate = Double.parseDouble(parts[2]);

            interestRuleService.addInterestRule(date, ruleId, rate);

            // Print all interest rules
            List<InterestRule> rules = interestRuleService.getAllInterestRules();

            System.out.println("Interest rules:");
            System.out.println("| Date     | RuleId | Rate (%) |");

            for (InterestRule rule : rules) {
                System.out.printf("| %s | %-6s | %8.2f |\n",
                        rule.getFormattedDate(), rule.getRuleId(), rule.getRate());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printStatement() {
        System.out.println("Please enter account and month to generate the statement <Account> <Year><Month>");
        System.out.println("(or enter blank to go back to main menu):");
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }

        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid input format");
            }

            String accountId = parts[0];
            String yearMonth = parts[1];

            if (yearMonth.length() != 6 || !yearMonth.matches("\\d{6}")) {
                throw new IllegalArgumentException("Year and month should be in YYYYMM format");
            }

            // Print statement
            List<Statement> statement = transactionService.generateMonthlyStatement(accountId, yearMonth);

            if (statement.isEmpty()) {
                System.out.println("No transactions found for the specified month.");
                return;
            }

            System.out.println("Account: " + accountId);
            System.out.println("| Date     | Txn Id      | Type | Amount | Balance |");

            for (Statement line : statement) {
                System.out.printf("| %s | %-11s | %-4s | %6.2f | %7.2f |\n",
                        line.getDate(),
                        line.getTransactionId() != null ? line.getTransactionId() : "",
                        line.getType(),
                        line.getAmount(),
                        line.getBalance());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        BankSystem aweSomeGicBank = new BankSystem();
        aweSomeGicBank.run();
    }
}