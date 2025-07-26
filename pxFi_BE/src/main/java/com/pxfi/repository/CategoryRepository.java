package com.pxfi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.pxfi.model.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
}