package com.pxfi.repository;

import com.pxfi.model.CategorizationRule;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorizationRuleRepository extends MongoRepository<CategorizationRule, String> {
    @Query("{ 'userId': ObjectId('?0') }")
    List<CategorizationRule> findByUserId(ObjectId userId);
}
