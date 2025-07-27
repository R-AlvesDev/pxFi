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
    List<Transaction> findByAccountIdOrderByBookingDateDesc(String accountId);

    List<Transaction> findByRemittanceInformationUnstructuredAndCategoryIdIsNull(String remittanceInfo);

    List<Transaction> findByRemittanceInformationUnstructuredAndCategoryIdAndSubCategoryIdIsNull(String remittanceInfo, String categoryId);

    boolean existsByTransactionIdAndAccountId(String transactionId, String accountId);

    @Query(value = "{ 'accountId': ?0, 'bookingDate': ?1, 'transactionAmount.amount': ?2, 'remittanceInformationUnstructured': ?3 }", exists = true)
    Boolean existsDuplicate(String accountId, String bookingDate, String amount, String remittance);

    @Query("{ 'accountId': ?0, 'bookingDate': ?1, 'transactionAmount.amount': ?2, 'remittanceInformationUnstructured': ?3, 'transactionId': null }")
    Optional<Transaction> findPotentialDuplicate(String accountId, String bookingDate, String amount, String remittance);

    @Aggregation(pipeline = {
        "{ '$match': { 'bookingDate': { '$gte': ?0, '$lte': ?1 }, 'transactionAmount.amount': { '$lt': '0' } } }",
        "{ '$group': { '_id': '$categoryName', 'total': { '$sum': { '$toDouble': '$transactionAmount.amount' } } } }",
        "{ '$project': { 'categoryName': '$_id', 'total': { '$abs': '$total' }, '_id': 0 } }"
    })
    List<CategorySpending> findSpendingByCategory(String startDate, String endDate);

    List<Transaction> findByBookingDateBetween(String startDate, String endDate);
}