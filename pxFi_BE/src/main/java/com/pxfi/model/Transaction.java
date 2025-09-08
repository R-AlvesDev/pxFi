package com.pxfi.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private ObjectId userId; 
    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    private String transactionId;
    private String internalTransactionId;
    private String debtorName;
    private DebtorAccount debtorAccount;
    private TransactionAmount transactionAmount;
    private String bookingDate;
    private String valueDate;
    private String remittanceInformationUnstructured;
    private String bankTransactionCode;
    private boolean ignored = false; 
    private String categoryId;
    private String subCategoryId;
    private String categoryName;
    private String subCategoryName;

    private String linkedTransactionId;

    @Field("accountId")
    private String accountId;

    public String getLinkedTransactionId() {
        return linkedTransactionId;
    }

    public void setLinkedTransactionId(String linkedTransactionId) {
        this.linkedTransactionId = linkedTransactionId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(String subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }


    // --- Existing getters and setters below ---

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public DebtorAccount getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(DebtorAccount debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public TransactionAmount getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(TransactionAmount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public String getBankTransactionCode() {
        return bankTransactionCode;
    }

    public void setBankTransactionCode(String bankTransactionCode) {
        this.bankTransactionCode = bankTransactionCode;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public String getInternalTransactionId() {
        return internalTransactionId;
    }

    public void setInternalTransactionId(String internalTransactionId) {
        this.internalTransactionId = internalTransactionId;
    }

}