package com.pxfi.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.pxfi.model.CategorizationRule;

@Repository
public interface CategorizationRuleRepository extends MongoRepository<CategorizationRule, String> {
    @Query("{ 'userId': ObjectId('?0') }")
    List<CategorizationRule> findByUserId(String userId);
}