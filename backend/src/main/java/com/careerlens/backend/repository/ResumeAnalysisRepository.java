package com.careerlens.backend.repository;

import com.careerlens.backend.model.ResumeAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends MongoRepository<ResumeAnalysis, String> {
    Optional<ResumeAnalysis> findByResumeId(String resumeId);
    Optional<ResumeAnalysis> findByJobSeekerId(String jobSeekerId);
    void deleteByResumeId(String resumeId);
}
