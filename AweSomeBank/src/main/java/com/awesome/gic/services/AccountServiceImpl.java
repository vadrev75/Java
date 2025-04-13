package com.awesome.gic.services;

import com.awesome.gic.interfaces.AccountService;
import com.awesome.gic.models.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountServiceImpl implements AccountService {
    private Map<String, Account> accounts;

    public AccountServiceImpl() {
        accounts = new HashMap<>();
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public Account createAccount(String accountId) {
        Account account = new Account(accountId);
        accounts.put(accountId, account);
        return account;
    }

    @Override
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }
}