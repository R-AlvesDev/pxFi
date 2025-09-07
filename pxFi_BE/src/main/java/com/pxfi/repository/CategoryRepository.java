package com.pxfi.repository;

import com.pxfi.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {

    @Query("{ 'userId': ObjectId('?0') }")
    List<Category> findByUserId(String userId);

    @Query("{ '_id': ?0, 'userId': ObjectId('?1') }")
    Optional<Category> findByIdAndUserId(String id, String userId);

    @Query(value = "{ 'parentId': ?0, 'userId': ObjectId('?1') }", count = true)
    long countByParentIdAndUserId(String parentId, String userId);
    
    boolean existsByParentId(String parentId);
}