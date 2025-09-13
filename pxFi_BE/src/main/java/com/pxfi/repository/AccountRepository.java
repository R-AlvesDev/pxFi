package com.pxfi.repository;

import com.pxfi.model.Account;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AccountRepository extends MongoRepository<Account, String> {
    // Find all accounts belonging to a specific user
    @Query("{ 'userId': ObjectId('?0') }")
    List<Account> findByUserId(ObjectId userId);
}