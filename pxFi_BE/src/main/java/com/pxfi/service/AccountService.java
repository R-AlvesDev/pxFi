package com.pxfi.service;

import com.pxfi.crypto.EncryptionService;
import com.pxfi.model.Account;
import com.pxfi.repository.AccountRepository;
import com.pxfi.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final EncryptionService encryptionService;
    private final TransactionRepository transactionRepository;

    public AccountService(
            AccountRepository accountRepository,
            EncryptionService encryptionService,
            TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
        this.transactionRepository = transactionRepository;
    }

    public List<Account> getAccountsByUserId(ObjectId userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::decryptAccount)
                .collect(Collectors.toList());
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(encryptAccount(account));
    }

    public Account updateAccountName(String accountId, String newName, ObjectId currentUserId) {
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        Account account = optionalAccount.get();

        // Security Check: Ensure the account belongs to the user trying to edit it
        if (!account.getUserId().equals(currentUserId)) {
            throw new SecurityException("User does not have permission to update this account.");
        }

        account.setAccountName(newName);

        Account updatedAccount = accountRepository.save(encryptAccount(account));

        return decryptAccount(updatedAccount);
    }

    @Transactional
    public void deleteAccount(String accountId, ObjectId currentUserId) {
        // First, verify the user owns this account
        Optional<Account> optionalAccount = accountRepository.findById(accountId);
        if (optionalAccount.isEmpty()) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }
        Account account = optionalAccount.get();
        if (!account.getUserId().equals(currentUserId)) {
            throw new SecurityException("User does not have permission to delete this account.");
        }

        // If ownership is confirmed, proceed with deletion
        transactionRepository.deleteByAccountId(account.getId());

        accountRepository.deleteById(account.getId());
    }

    public Account encryptAccount(Account acc) {
        if (acc == null) {
            return null;
        }
        acc.setAccountName(encryptionService.encrypt(acc.getAccountName()));
        return acc;
    }

    public Account decryptAccount(Account acc) {
        if (acc == null) {
            return null;
        }
        acc.setAccountName(encryptionService.decrypt(acc.getAccountName()));
        return acc;
    }
}
