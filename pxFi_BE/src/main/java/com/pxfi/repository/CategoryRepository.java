package com.pxfi.repository;

import com.pxfi.model.Category;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CategoryRepository extends MongoRepository<Category, String> {

    @Query("{ 'userId': ObjectId('?0') }")
    List<Category> findByUserId(ObjectId userId);

    @Query("{ '_id': ?0, 'userId': ObjectId('?1') }")
    Optional<Category> findByIdAndUserId(String id, ObjectId userId);

    @Query(value = "{ 'parentId': ?0, 'userId': ObjectId('?1') }", count = true)
    long countByParentIdAndUserId(String parentId, ObjectId userId);

    boolean existsByParentId(String parentId);
}
