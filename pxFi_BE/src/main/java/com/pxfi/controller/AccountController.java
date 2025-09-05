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
            return ResponseEntity.status(401).build();
        }
        List<Account> accounts = accountService.getAccountsByUserId(currentUser.getId());
        return ResponseEntity.ok(accounts);
    }
}