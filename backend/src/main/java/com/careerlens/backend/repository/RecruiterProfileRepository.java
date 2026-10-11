package com.careerlens.backend.repository;

import com.careerlens.backend.model.RecruiterProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecruiterProfileRepository extends MongoRepository<RecruiterProfile, String> {
    Optional<RecruiterProfile> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
