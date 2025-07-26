package com.pxfi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pxfi.model.CategorizationRule;

@Repository
public interface CategorizationRuleRepository extends MongoRepository<CategorizationRule, String> {
}