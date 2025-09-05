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
        return accountRepository.findByUserId(userId);
    }

    public Account saveAccount(Account account) {
        // Here you could add logic to prevent duplicate accounts for the same user
        return accountRepository.save(account);
    }
}