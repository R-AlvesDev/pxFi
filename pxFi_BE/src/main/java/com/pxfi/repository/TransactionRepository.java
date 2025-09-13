package com.pxfi.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.pxfi.model.CategorySpending;
import com.pxfi.model.Transaction;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    @Query("{'accountId': ?0, 'userId': ObjectId('?1')}")
    List<Transaction> findByAccountIdAndUserIdOrderByBookingDateDesc(String accountId, ObjectId userId);

    boolean existsByTransactionIdAndAccountId(String transactionId, String accountId);

    @Query(value = "{ 'accountId': ?0, 'bookingDate': ?1, 'transactionAmount.amount': ?2, 'remittanceInformationUnstructured': ?3 }", exists = true)
    Boolean existsDuplicate(String accountId, String bookingDate, String amount, String remittance);

    @Query("{ 'accountId': ?0, 'bookingDate': ?1, 'transactionAmount.amount': ?2, 'remittanceInformationUnstructured': ?3, 'transactionId': null }")
    Optional<Transaction> findPotentialDuplicate(String accountId, String bookingDate, String amount, String remittance);

    @Aggregation(pipeline = {
        "{ '$match': { " +
            "'userId': ObjectId('?0'), " +
            "'accountId': ?1, " +
            "'bookingDate': { '$gte': ?2, '$lte': ?3 }, " +
            "'transactionAmount.amount': { '$lt': '0' }, " +
            "'ignored': false, " +
            "'categoryId': { '$ne': null } " +
        "} }",
        "{ '$lookup': { 'from': 'categories', 'localField': 'categoryId', 'foreignField': '_id', 'as': 'categoryInfo' } }",
        "{ '$unwind': '$categoryInfo' }",
        "{ '$match': { 'categoryInfo.isAssetTransfer': false } }",
        "{ '$group': { '_id': '$categoryName', 'total': { '$sum': { '$toDouble': '$transactionAmount.amount' } } } }",
        "{ '$project': { 'categoryName': '$_id', 'total': { '$abs': '$total' }, '_id': 0 } }"
    })
    List<CategorySpending> findSpendingByCategory(ObjectId userId, String accountId, String startDate, String endDate);

    List<Transaction> findByBookingDateBetween(String startDate, String endDate);

    List<Transaction> findByRemittanceInformationUnstructuredAndCategoryIdIsNull(String remittanceInfo);

    List<Transaction> findByRemittanceInformationUnstructuredAndCategoryIdAndSubCategoryIdIsNull(
        String remittanceInfo, String categoryId
    );
    
    @Query("{'_id': ?0, 'userId': ObjectId('?1')}")
    Optional<Transaction> findByIdAndUserId(String id, ObjectId userId);

    @Query("{'userId': ObjectId('?0')}")
    List<Transaction> findAllByUserId(ObjectId userId);
    
    @Query("{'accountId': ?0, 'userId': ObjectId('?1'), 'bookingDate': {'$gte': ?2, '$lte': ?3}}")
    List<Transaction> findByAccountIdAndUserIdAndBookingDateBetweenOrderByBookingDateDesc(String accountId, ObjectId userId, String startDate, String endDate);

}