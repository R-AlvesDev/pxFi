package com.pxfi.controller;

import com.pxfi.model.Account;
import com.pxfi.model.User;
import com.pxfi.security.SecurityConfiguration;
import com.pxfi.service.AccountService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts() {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<Account> accounts = accountService.getAccountsByUserId(currentUser.getId());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<Account> saveAccount(@RequestBody Account account) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        account.setUserId(currentUser.getId());
        Account savedAccount = accountService.saveAccount(account);
        return ResponseEntity.ok(savedAccount);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<Account> updateAccountName(
            @PathVariable String accountId, @RequestBody Map<String, String> payload) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        String newName = payload.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Account updatedAccount = accountService.updateAccountName(accountId, newName, currentUser.getId());
            return ResponseEntity.ok(updatedAccount);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountId) {
        User currentUser = SecurityConfiguration.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            accountService.deleteAccount(accountId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
