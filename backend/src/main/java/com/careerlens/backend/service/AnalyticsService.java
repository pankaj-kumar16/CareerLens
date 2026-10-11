package com.careerlens.backend.service;

import com.careerlens.backend.dto.JobPerformanceData;
import com.careerlens.backend.dto.RecruiterAnalyticsResponse;
import com.careerlens.backend.dto.TrendDataPoint;
import com.careerlens.backend.model.Application;
import com.careerlens.backend.model.ApplicationStatus;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.Job;
import com.careerlens.backend.model.MatchResult;
import com.careerlens.backend.repository.ApplicationRepository;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.JobRepository;
import com.careerlens.backend.repository.MatchResultRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchResultRepository matchResultRepository;
    private final CompanyRepository companyRepository;

    public AnalyticsService(JobRepository jobRepository,
                            ApplicationRepository applicationRepository,
                            MatchResultRepository matchResultRepository,
                            CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.matchResultRepository = matchResultRepository;
        this.companyRepository = companyRepository;
    }

    public RecruiterAnalyticsResponse getRecruiterAnalytics(String recruiterId, int days) {
        List<Job> recruiterJobs = jobRepository.findByRecruiterId(recruiterId);
        if (recruiterJobs.isEmpty()) {
            return new RecruiterAnalyticsResponse(0, 0, 0, 0, 0, 0, List.of(), List.of());
        }

        long activeJobsCount = recruiterJobs.stream().filter(Job::isActive).count();
        List<String> jobIds = recruiterJobs.stream().map(Job::getId).toList();

        List<Application> allApps = applicationRepository.findByJobIdIn(jobIds);

        long totalApplicants = allApps.size();
        long shortlisted = allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
        long interviews = allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW).count();
        long selected = allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count();
        long rejected = allApps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

        // Application Trend over last N days
        List<TrendDataPoint> trend = calculateApplicationTrend(allApps, days);

        // Job Performance Breakdown
        List<JobPerformanceData> performanceList = new ArrayList<>();
        for (Job job : recruiterJobs) {
            List<Application> jobApps = allApps.stream().filter(a -> a.getJobId().equals(job.getId())).toList();

            long jTotal = jobApps.size();
            long jShort = jobApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count();
            long jInter = jobApps.stream().filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW).count();
            long jSelect = jobApps.stream().filter(a -> a.getStatus() == ApplicationStatus.SELECTED).count();
            long jReject = jobApps.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();

            // Calculate average match score for job
            List<MatchResult> matches = matchResultRepository.findByJobId(job.getId());
            double avgMatch = matches.isEmpty() ? 0.0 :
                    matches.stream().mapToDouble(MatchResult::getOverallScore).average().orElse(0.0);

            String companyName = "Company";
            if (job.getCompanyId() != null && !job.getCompanyId().trim().isEmpty()) {
                companyName = companyRepository.findById(job.getCompanyId())
                        .map(Company::getName).orElse("Company");
            }

            performanceList.add(new JobPerformanceData(
                    job.getId(),
                    job.getTitle(),
                    companyName,
                    job.isActive(),
                    jTotal,
                    jShort,
                    jInter,
                    jSelect,
                    jReject,
                    Math.round(avgMatch * 10.0) / 10.0
            ));
        }

        // Sort performance list by total applicants descending
        performanceList.sort((a, b) -> Long.compare(b.getTotalApplicants(), a.getTotalApplicants()));

        return new RecruiterAnalyticsResponse(
                activeJobsCount,
                totalApplicants,
                shortlisted,
                interviews,
                selected,
                rejected,
                trend,
                performanceList
        );
    }

    private List<TrendDataPoint> calculateApplicationTrend(List<Application> applications, int days) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate startDate = today.minusDays(days - 1);

        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Pre-fill last N dates with 0
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            dateCountMap.put(d.format(formatter), 0L);
        }

        for (Application app : applications) {
            if (app.getAppliedAt() != null) {
                LocalDate appDate = app.getAppliedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                String formatted = appDate.format(formatter);
                if (dateCountMap.containsKey(formatted)) {
                    dateCountMap.put(formatted, dateCountMap.get(formatted) + 1);
                }
            }
        }

        List<TrendDataPoint> points = new ArrayList<>();
        for (Map.Entry<String, Long> entry : dateCountMap.entrySet()) {
            points.add(new TrendDataPoint(entry.getKey(), entry.getValue()));
        }

        return points;
    }
}
