package com.pxfi.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pxfi.model.EndUserAgreementRequest;
import com.pxfi.model.EndUserAgreementResponse;
import com.pxfi.model.RequisitionDetailsResponse;
import com.pxfi.model.RequisitionResponse;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.model.UpdateCategoryRequest;
import com.pxfi.service.GoCardlessService;
import com.pxfi.service.TransactionService;

@RestController
@RequestMapping("/api")
public class GoCardlessController {

    private final GoCardlessService goCardlessService;
    private final TransactionService transactionService;

    public GoCardlessController(GoCardlessService goCardlessService, TransactionService transactionService) {
        this.goCardlessService = goCardlessService;
        this.transactionService = transactionService;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header.");
        }
        return authorizationHeader.substring(7);
    }

    @GetMapping("/access-token")
    public ResponseEntity<Map<String, String>> getAccessToken() throws Exception {
        String accessToken = goCardlessService.fetchAccessToken();
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/institutions")
    public ResponseEntity<List<Map<String, Object>>> listInstitutions(
            @RequestParam String accessToken,
            @RequestParam String countryCode) {

        List<Map<String, Object>> institutions = goCardlessService.getInstitutions(accessToken, countryCode);
        return ResponseEntity.ok(institutions);
    }

    @PostMapping("/agreements/enduser")
    public ResponseEntity<EndUserAgreementResponse> createEndUserAgreement(
            @RequestBody EndUserAgreementRequest request,
            @RequestHeader("Authorization") String authorization) throws Exception {

        String accessToken = extractToken(authorization);
        EndUserAgreementResponse response = goCardlessService.createEndUserAgreement(request.getInstitution_id(), accessToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requisitions/create")
    public ResponseEntity<RequisitionResponse> createRequisition(
            @RequestBody Map<String, String> payload,
            @RequestHeader("Authorization") String authorization) throws Exception {

        String accessToken = extractToken(authorization);
        String institutionId = payload.get("institutionId");
        String agreementId = payload.get("agreementId");

        RequisitionResponse response = goCardlessService.createRequisition(accessToken, institutionId, agreementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requisitions/details")
    public ResponseEntity<RequisitionDetailsResponse> getRequisitionDetails(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String requisitionId) throws Exception {

        String accessToken = extractToken(authorization);
        RequisitionDetailsResponse response = goCardlessService.getRequisitionDetails(accessToken, requisitionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String accountId) throws Exception {

        List<Transaction> cachedTransactions = transactionService.getTransactionsByAccountId(accountId);
        System.out.println("Found " + cachedTransactions.size() + " transactions in Mongo for accountId " + accountId);

        if (cachedTransactions != null && !cachedTransactions.isEmpty()) {
            return ResponseEntity.ok(cachedTransactions);
        }

        // If cache empty, fetch from API and save
        String accessToken = extractToken(authorization);
        TransactionsResponse apiResponse = goCardlessService.getAccountTransactions(accessToken, accountId);
        transactionService.saveTransactions(accountId, apiResponse);

        // Return just the booked + pending as a single list
        List<Transaction> allTxs = new ArrayList<>();
        if (apiResponse.getTransactions() != null) {
            if (apiResponse.getTransactions().getBooked() != null) {
                allTxs.addAll(apiResponse.getTransactions().getBooked());
            }
            if (apiResponse.getTransactions().getPending() != null) {
                allTxs.addAll(apiResponse.getTransactions().getPending());
            }
        }
        return ResponseEntity.ok(allTxs);
    }

    @PostMapping("/accounts/{accountId}/transactions/refresh")
    public ResponseEntity<List<Transaction>> refreshTransactions(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String accountId) throws Exception {

        String accessToken = extractToken(authorization);
        TransactionsResponse freshData = goCardlessService.getAccountTransactions(accessToken, accountId);
        transactionService.saveTransactions(accountId, freshData);

        // Return updated list from DB after saving
        List<Transaction> updatedTransactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(updatedTransactions);
    }

    @GetMapping("/debug/transactions/{accountId}")
    public List<Transaction> debugFetch(@PathVariable String accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    @GetMapping("/debug/all-transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PostMapping("/transactions/{id}/category")
    public ResponseEntity<Transaction> updateTransactionCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequest request) {
        Transaction updatedTransaction = transactionService.updateTransactionCategory(
            id, request.getCategoryId(), request.getSubCategoryId()
        );
        if (updatedTransaction != null) {
            return ResponseEntity.ok(updatedTransaction);
        }
        return ResponseEntity.notFound().build();
    }
}
