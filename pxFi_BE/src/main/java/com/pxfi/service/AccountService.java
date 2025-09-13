package com.pxfi.service;

import com.pxfi.crypto.EncryptionService;
import com.pxfi.model.Account;
import com.pxfi.repository.AccountRepository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final EncryptionService encryptionService;

    public AccountService(AccountRepository accountRepository, EncryptionService encryptionService) {
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
    }

    public List<Account> getAccountsByUserId(ObjectId userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::decryptAccount)
                .collect(Collectors.toList());
    }

    public Account saveAccount(Account account) {
        // Encrypt before saving
        return accountRepository.save(encryptAccount(account));
    }

    public Account encryptAccount(Account acc) {
        if (acc == null) return null;
        acc.setAccountName(encryptionService.encrypt(acc.getAccountName()));
        return acc;
    }

    public Account decryptAccount(Account acc) {
        if (acc == null) return null;
        acc.setAccountName(encryptionService.decrypt(acc.getAccountName()));
        return acc;
    }
}

