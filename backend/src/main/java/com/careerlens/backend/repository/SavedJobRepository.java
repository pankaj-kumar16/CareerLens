package com.careerlens.backend.repository;

import com.careerlens.backend.model.SavedJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends MongoRepository<SavedJob, String> {
    boolean existsByJobSeekerIdAndJobId(String jobSeekerId, String jobId);
    Optional<SavedJob> findByJobSeekerIdAndJobId(String jobSeekerId, String jobId);
    List<SavedJob> findByJobSeekerIdOrderBySavedAtDesc(String jobSeekerId);
    void deleteByJobSeekerIdAndJobId(String jobSeekerId, String jobId);
}
