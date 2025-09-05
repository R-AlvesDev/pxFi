package com.pxfi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.pxfi.model.CategorySpending;
import com.pxfi.model.Transaction;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByAccountIdAndUserIdOrderByBookingDateDesc(String accountId, String userId);

    boolean existsByTransactionIdAndAccountId(String transactionId, String accountId);

    @Query(value = "{ 'accountId': ?0, 'bookingDate': ?1, 'transactionAmount.amount': ?2, 'remittanceInformationUnstructured': ?3 }", exists = true)
    Boolean existsDuplicate(String accountId, String bookingDate, String amount, String remittance);

    @Query("{ 'accountId': ?0, 'bookingDate': ?1, 'transactionAmount.amount': ?2, 'remittanceInformationUnstructured': ?3, 'transactionId': null }")
    Optional<Transaction> findPotentialDuplicate(String accountId, String bookingDate, String amount, String remittance);

    @Aggregation(pipeline = {
        // Stage 1: Initial match on transactions
        "{ '$match': { " +
            "'bookingDate': { '$gte': ?0, '$lte': ?1 }, " +
            "'transactionAmount.amount': { '$lt': '0' }, " +
            "'ignored': false, " +
            "'categoryId': { '$ne': null } " +
        "} }",
        // Stage 2: Join with the categories collection
        "{ '$lookup': { " +
            "'from': 'categories', " +
            "'localField': 'categoryId', " +
            "'foreignField': '_id', " +
            "'as': 'categoryInfo' " +
        "} }",
        // Stage 3: Deconstruct the resulting array
        "{ '$unwind': '$categoryInfo' }",
        // Stage 4: Filter out the asset transfers
        "{ '$match': { 'categoryInfo.isAssetTransfer': false } }",
        // Stage 5: Group and sum the remaining expenses
        "{ '$group': { " +
            "'_id': '$categoryName', " +
            "'total': { '$sum': { '$toDouble': '$transactionAmount.amount' } } " +
        "} }",
        // Stage 6: Format the final output
        "{ '$project': { " +
            "'categoryName': '$_id', " +
            "'total': { '$abs': '$total' }, " +
            "'_id': 0 " +
        "} }"
    })
    List<CategorySpending> findSpendingByCategory(String startDate, String endDate);

    List<Transaction> findByBookingDateBetween(String startDate, String endDate);

    List<Transaction> findByRemittanceInformationUnstructuredAndCategoryIdIsNull(String remittanceInfo);

    List<Transaction> findByRemittanceInformationUnstructuredAndCategoryIdAndSubCategoryIdIsNull(
        String remittanceInfo, String categoryId
    );

    Optional<Transaction> findByIdAndUserId(String id, String userId);

    List<Transaction> findAllByUserId(String userId);
    
    List<Transaction> findByAccountIdAndUserIdAndBookingDateBetweenOrderByBookingDateDesc(String accountId, String userId, String startDate, String endDate);

}