package com.pxfi.controller;

import com.pxfi.model.CategorizeSimilarRequest;
import com.pxfi.model.EndUserAgreementRequest;
import com.pxfi.model.EndUserAgreementResponse;
import com.pxfi.model.LinkTransactionsRequest;
import com.pxfi.model.RequisitionDetailsResponse;
import com.pxfi.model.RequisitionResponse;
import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.model.UpdateCategoryRequest;
import com.pxfi.service.GoCardlessService;
import com.pxfi.service.TransactionService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GoCardlessController {

    private final GoCardlessService goCardlessService;
    private final TransactionService transactionService;

    public GoCardlessController(
            GoCardlessService goCardlessService, TransactionService transactionService) {
        this.goCardlessService = goCardlessService;
        this.transactionService = transactionService;
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
            @RequestParam String accessToken, @RequestParam String countryCode) {

        List<Map<String, Object>> institutions = goCardlessService.getInstitutions(accessToken, countryCode);
        return ResponseEntity.ok(institutions);
    }

    @PostMapping("/agreements/enduser")
    public ResponseEntity<EndUserAgreementResponse> createEndUserAgreement(
            @RequestBody EndUserAgreementRequest request, @RequestParam String gocardlessToken)
            throws Exception {

        EndUserAgreementResponse response = goCardlessService.createEndUserAgreement(
                request.getInstitutionId(), gocardlessToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/requisitions/create")
    public ResponseEntity<RequisitionResponse> createRequisition(
            @RequestBody Map<String, String> payload, @RequestParam String gocardlessToken)
            throws Exception {

        String institutionId = payload.get("institutionId");
        String agreementId = payload.get("agreementId");

        RequisitionResponse response = goCardlessService.createRequisition(gocardlessToken, institutionId, agreementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requisitions/details")
    public ResponseEntity<RequisitionDetailsResponse> getRequisitionDetails(
            @RequestParam String gocardlessToken, @RequestParam String requisitionId)
            throws Exception {

        RequisitionDetailsResponse response = goCardlessService.getRequisitionDetails(gocardlessToken, requisitionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(
            @PathVariable String accountId,
            @RequestParam Optional<String> startDate,
            @RequestParam Optional<String> endDate) {

        List<Transaction> cachedTransactions = transactionService.getTransactionsByAccountId(
                accountId, startDate.orElse(null), endDate.orElse(null));

        return ResponseEntity.ok(cachedTransactions);
    }

    @PostMapping("/accounts/{accountId}/transactions/refresh")
    public ResponseEntity<List<Transaction>> refreshTransactions(@PathVariable String accountId)
            throws Exception {
        String gocardlessToken = goCardlessService.fetchAccessToken();
        TransactionsResponse freshData = goCardlessService.getAccountTransactions(gocardlessToken, accountId);
        transactionService.saveTransactions(accountId, freshData);

        List<Transaction> updatedTransactions = transactionService.getTransactionsByAccountId(accountId, null, null);
        return ResponseEntity.ok(updatedTransactions);
    }

    @GetMapping("/debug/transactions/{accountId}")
    public List<Transaction> debugFetch(@PathVariable String accountId) {
        return transactionService.getTransactionsByAccountId(accountId, null, null);
    }

    @GetMapping("/debug/all-transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @PostMapping("/transactions/{id}/category")
    public ResponseEntity<Transaction> updateTransactionCategory(
            @PathVariable String id, @RequestBody UpdateCategoryRequest request) {
        Transaction updatedTransaction = transactionService.updateTransactionCategory(
                id, request.getCategoryId(), request.getSubCategoryId());
        if (updatedTransaction != null) {
            return ResponseEntity.ok(updatedTransaction);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/transactions/categorize-similar")
    public ResponseEntity<List<Transaction>> categorizeSimilarTransactions(
            @RequestBody CategorizeSimilarRequest request) {
        List<Transaction> updatedTransactions = transactionService.categorizeSimilarTransactions(
                request.getRemittanceInfo(),
                request.getCategoryId(),
                request.getSubCategoryId(),
                request.isAddingSubcategory());
        return ResponseEntity.ok(updatedTransactions);
    }

    @PostMapping("/transactions/{id}/toggle-ignore")
    public ResponseEntity<Transaction> toggleIgnore(@PathVariable String id) {
        return transactionService
                .toggleTransactionIgnoreStatus(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/transactions/link")
    public ResponseEntity<Void> linkTransactions(@RequestBody LinkTransactionsRequest request) {
        try {
            transactionService.linkTransactions(request.getExpenseId(), request.getIncomeId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
