package com.pxfi.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pxfi.model.CategorizationRule;

@Repository
public interface CategorizationRuleRepository extends MongoRepository<CategorizationRule, String> {
    List<CategorizationRule> findByUserId(String userId);

}