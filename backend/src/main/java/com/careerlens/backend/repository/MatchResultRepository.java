package com.careerlens.backend.repository;

import com.careerlens.backend.model.MatchResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends MongoRepository<MatchResult, String> {
    Optional<MatchResult> findByResumeIdAndJobId(String resumeId, String jobId);
    List<MatchResult> findByJobSeekerId(String jobSeekerId);
    List<MatchResult> findByJobId(String jobId);
    List<MatchResult> findByJobIdOrderByOverallScoreDesc(String jobId);
    void deleteByJobId(String jobId);
    void deleteByResumeId(String resumeId);
}
