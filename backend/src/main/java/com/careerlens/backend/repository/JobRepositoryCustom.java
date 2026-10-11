package com.careerlens.backend.repository;

import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobRepositoryCustom {
    Page<Job> searchJobs(String keyword, List<String> skills, String location,
                         WorkMode workMode, JobType jobType, Integer minExperience,
                         Double minSalary, String companyId, Pageable pageable);
}
