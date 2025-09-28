package com.pxfi.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pxfi.model.User;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findByUsername(String username);
}
