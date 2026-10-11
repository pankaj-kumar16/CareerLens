package com.careerlens.backend.repository;

import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.JobType;
import com.careerlens.backend.model.WorkMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JobRepositoryCustomImpl implements JobRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public JobRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Job> searchJobs(String keyword, List<String> skills, String location,
                                 WorkMode workMode, JobType jobType, Integer minExperience,
                                 Double minSalary, String companyId, Pageable pageable) {

        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("active").is(true));

        if (StringUtils.hasText(keyword)) {
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(keyword, "i"),
                    Criteria.where("description").regex(keyword, "i")
            );
            criteriaList.add(keywordCriteria);
        }

        if (skills != null && !skills.isEmpty()) {
            List<String> regexSkills = skills.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList();
            if (!regexSkills.isEmpty()) {
                Criteria skillsCriteria = new Criteria().orOperator(
                        Criteria.where("requiredSkills").in(regexSkills),
                        Criteria.where("preferredSkills").in(regexSkills)
                );
                criteriaList.add(skillsCriteria);
            }
        }

        if (StringUtils.hasText(location)) {
            criteriaList.add(Criteria.where("location").regex(location, "i"));
        }

        if (workMode != null) {
            criteriaList.add(Criteria.where("workMode").is(workMode));
        }

        if (jobType != null) {
            criteriaList.add(Criteria.where("jobType").is(jobType));
        }

        if (minExperience != null) {
            criteriaList.add(Criteria.where("minimumExperience").lte(minExperience));
        }

        if (minSalary != null) {
            criteriaList.add(Criteria.where("salaryMax").gte(minSalary));
        }

        if (StringUtils.hasText(companyId)) {
            criteriaList.add(Criteria.where("companyId").is(companyId));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Job.class);
        query.with(pageable);
        List<Job> jobs = mongoTemplate.find(query, Job.class);

        return new PageImpl<>(jobs, pageable, total);
    }
}
