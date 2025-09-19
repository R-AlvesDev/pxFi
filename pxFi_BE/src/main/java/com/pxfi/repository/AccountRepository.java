package com.pxfi.repository;

import com.pxfi.model.Account;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AccountRepository extends MongoRepository<Account, String> {
    @Query("{ 'userId': ObjectId('?0') }")
    List<Account> findByUserId(ObjectId userId);
}
