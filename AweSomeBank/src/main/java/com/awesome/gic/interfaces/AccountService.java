package com.awesome.gic.interfaces;

import com.awesome.gic.models.Account;

import java.util.List;

public interface AccountService {
    Account getAccount(String accountId);
    Account createAccount(String accountId);
    List<Account> getAllAccounts();
}
