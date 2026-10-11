package com.careerlens.backend.repository;

import com.careerlens.backend.model.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {
    Optional<Resume> findByJobSeekerId(String jobSeekerId);
    List<Resume> findByJobSeekerIdOrderByUploadedAtDesc(String jobSeekerId);
    Optional<Resume> findByJobSeekerIdAndIsDefaultTrue(String jobSeekerId);
    Optional<Resume> findByIdAndJobSeekerId(String id, String jobSeekerId);
    long countByJobSeekerId(String jobSeekerId);
    void deleteByJobSeekerId(String jobSeekerId);
}
