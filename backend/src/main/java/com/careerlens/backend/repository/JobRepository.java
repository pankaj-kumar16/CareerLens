package com.careerlens.backend.repository;

import com.careerlens.backend.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends MongoRepository<Job, String>, JobRepositoryCustom {
    Page<Job> findByRecruiterId(String recruiterId, Pageable pageable);
    List<Job> findByRecruiterId(String recruiterId);
    Optional<Job> findByIdAndRecruiterId(String id, String recruiterId);
    Optional<Job> findByIdAndActiveTrue(String id);
}
