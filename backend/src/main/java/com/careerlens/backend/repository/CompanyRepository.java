package com.careerlens.backend.repository;

import com.careerlens.backend.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CompanyRepository extends MongoRepository<Company, String> {
    Optional<Company> findByUserId(String userId);
    Optional<Company> findByName(String name);
    Page<Company> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
