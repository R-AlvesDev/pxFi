package com.pxfi.service;

import com.pxfi.model.Account;
import com.pxfi.repository.AccountRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAccountsByUserId(String userId) {
        System.out.println("[LOG] AccountService: Fetching accounts for userId='" + userId + "'");
        List<Account> accounts = accountRepository.findByUserId(userId);
        System.out.println("[LOG] AccountService: Found " + accounts.size() + " accounts.");
        return accounts;
    }

    public Account saveAccount(Account account) {
        // Here you could add logic to prevent duplicate accounts for the same user
        System.out.println("[LOG] AccountService: Saving account for userId='" + account.getUserId() + "'");
        return accountRepository.save(account);
    }
}