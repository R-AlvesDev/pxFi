package com.pxfi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pxfi.model.Transaction;
import com.pxfi.model.TransactionsResponse;
import com.pxfi.repository.TransactionRepository;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public void saveTransactions(String accountId, TransactionsResponse transactionsResponse) {
        if (transactionsResponse.getTransactions() == null) return;

        List<Transaction> allTxs = new ArrayList<>();

        List<Transaction> booked = transactionsResponse.getTransactions().getBooked();
        List<Transaction> pending = transactionsResponse.getTransactions().getPending();

        if (booked != null) {
            for (Transaction tx : booked) {
                tx.setAccountId(accountId);
                if (isNewTransaction(accountId, tx)) {
                    allTxs.add(tx);
                }
            }
        }

        if (pending != null) {
            for (Transaction tx : pending) {
                tx.setAccountId(accountId);
                if (isNewTransaction(accountId, tx)) {
                    allTxs.add(tx);
                }
            }
        }

        if (!allTxs.isEmpty()) {
            transactionRepository.saveAll(allTxs);
        }
    }

    private boolean isNewTransaction(String accountId, Transaction tx) {
        String transactionId = tx.getTransactionId();
        if (transactionId != null) {
            return !transactionRepository.existsByTransactionIdAndAccountId(transactionId, accountId);
        } else {
            String amount = tx.getTransactionAmount() != null ? tx.getTransactionAmount().getAmount() : null;
            String date = tx.getBookingDate();
            String remittance = tx.getRemittanceInformationUnstructured();

            if (amount != null && date != null && remittance != null) {
                // Safely handle the Boolean return type
                Boolean exists = transactionRepository.existsDuplicate(accountId, date, amount, remittance);
                return !Boolean.TRUE.equals(exists);
            } else {
                // If any of the fallback fields are null, assume it's a new transaction
                return true;
            }
        }
    }

    public List<Transaction> getTransactionsByAccountId(String accountId) {
        List<Transaction> results = transactionRepository.findByAccountId(accountId);
        System.out.println("Found " + results.size() + " transactions for accountId: " + accountId);
        return results;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

}
