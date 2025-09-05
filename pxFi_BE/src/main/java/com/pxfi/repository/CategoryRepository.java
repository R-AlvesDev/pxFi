package com.pxfi.repository;

import com.pxfi.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {

    // Finds all categories belonging to a specific user.
    List<Category> findByUserId(String userId);

    // Finds a single category by its ID, but only if it belongs to the specified user.
    // This is crucial for security in update and delete operations.
    Optional<Category> findByIdAndUserId(String id, String userId);

    // Counts how many subcategories a given category has, scoped to the user.
    // This prevents a user from deleting a category that still contains subcategories.
    long countByParentIdAndUserId(String parentId, String userId);
    
    // This method is no longer needed with the new user-aware logic,
    // but I'm leaving it here in case it's used in other parts of your code.
    // If it's not used elsewhere, you can safely delete it.
    boolean existsByParentId(String parentId);
}