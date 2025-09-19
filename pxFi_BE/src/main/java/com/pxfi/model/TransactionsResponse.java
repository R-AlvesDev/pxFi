package com.pxfi.model;

import java.util.List;

public class TransactionsResponse {
    private Transactions transactions;

    public static class Transactions {
        private List<Transaction> booked;
        private List<Transaction> pending;

        public List<Transaction> getBooked() {
            return booked;
        }

        public void setBooked(List<Transaction> booked) {
            this.booked = booked;
        }

        public List<Transaction> getPending() {
            return pending;
        }

        public void setPending(List<Transaction> pending) {
            this.pending = pending;
        }
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public void setTransactions(Transactions transactions) {
        this.transactions = transactions;
    }
}
