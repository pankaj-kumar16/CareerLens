package com.careerlens.backend.repository;

import com.careerlens.backend.model.Application;
import com.careerlens.backend.model.ApplicationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    boolean existsByJobIdAndJobSeekerId(String jobId, String jobSeekerId);
    Optional<Application> findByJobIdAndJobSeekerId(String jobId, String jobSeekerId);
    List<Application> findByJobSeekerIdOrderByAppliedAtDesc(String jobSeekerId);
    Optional<Application> findByIdAndJobSeekerId(String id, String jobSeekerId);
    List<Application> findByJobIdOrderByAppliedAtDesc(String jobId);
    List<Application> findByJobIdIn(List<String> jobIds);
    List<Application> findByJobIdInOrderByAppliedAtDesc(List<String> jobIds);
    List<Application> findByJobIdInAndStatusOrderByAppliedAtDesc(List<String> jobIds, ApplicationStatus status);
}
