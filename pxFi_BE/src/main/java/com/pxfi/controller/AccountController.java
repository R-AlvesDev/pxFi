package com.pxfi.controller;

import com.pxfi.model.Account;
import com.pxfi.model.User;
import com.pxfi.security.SecurityConfiguration;
import com.pxfi.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
            System.out.println("[LOG] AccountController: Cannot get accounts, user is not authenticated.");
            return ResponseEntity.status(401).build();
        }
        System.out.println("[LOG] AccountController: Authenticated user is '" + currentUser.getUsername() + "' with ID '" + currentUser.getId() + "'");
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
}