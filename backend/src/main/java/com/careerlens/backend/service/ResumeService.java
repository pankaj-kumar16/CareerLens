package com.careerlens.backend.service;

import com.careerlens.backend.dto.ResumeResponse;
import com.careerlens.backend.exception.BadRequestException;
import com.careerlens.backend.exception.ResourceNotFoundException;
import com.careerlens.backend.model.ParsingStatus;
import com.careerlens.backend.model.Resume;
import com.careerlens.backend.repository.ResumeRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final FileStorageService fileStorageService;

    private static final List<String> COMMON_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "Spring", "React", "Node.js", "JavaScript", "TypeScript",
            "Python", "Django", "FastAPI", "Flask", "C++", "C#", "SQL", "MongoDB", "PostgreSQL", "MySQL",
            "Docker", "Kubernetes", "Terraform", "Ansible", "Jenkins", "CI/CD", "DevOps", "AWS", "Azure", "GCP",
            "HTML", "CSS", "Tailwind CSS", "Bootstrap", "Git", "GitHub", "REST API", "Microservices", "GraphQL",
            "JUnit", "Mockito", "Maven", "Gradle", "Linux", "Bash", "YAML", "Agile", "Scrum",
            "Redux", "Express", "Next.js", "Angular", "Vue", "Redis", "Kafka", "Machine Learning", "Pandas", "NumPy"
    );

    public ResumeService(ResumeRepository resumeRepository,
                         FileStorageService fileStorageService) {
        this.resumeRepository = resumeRepository;
        this.fileStorageLocation = null;
        this.fileStorageService = fileStorageService;
    }

    private final Path fileStorageLocation;

    public ResumeResponse uploadResumeVersion(String jobSeekerId, String title, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload.");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFileName.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
        }

        String extension = getFileExtension(originalFileName).toLowerCase();
        if (!extension.equals("pdf") && !extension.equals("docx") && !extension.equals("doc")) {
            throw new BadRequestException("Only PDF and DOCX files are supported.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 10MB limit.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file bytes " + originalFileName, ex);
        }

        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storagePath;
        try {
            storagePath = fileStorageService.storeFile(new ByteArrayInputStream(bytes), originalFileName, storedFileName);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to store file " + originalFileName, ex);
        }

        String extractedText = "";
        ParsingStatus status = ParsingStatus.PENDING;
        try {
            extractedText = extractTextFromStream(new ByteArrayInputStream(bytes), extension);
            status = StringUtils.hasText(extractedText) ? ParsingStatus.SUCCESS : ParsingStatus.FAILED;
        } catch (Exception e) {
            status = ParsingStatus.FAILED;
        }

        List<String> skills = extractSkills(extractedText);
        List<String> education = extractEducation(extractedText);
        List<String> experience = extractExperience(extractedText);
        List<String> projects = extractProjects(extractedText);
        List<String> certifications = extractCertifications(extractedText);
        String summary = extractSummary(extractedText);

        boolean isFirstResume = resumeRepository.countByJobSeekerId(jobSeekerId) == 0;
        String resumeTitle = StringUtils.hasText(title) ? title.trim() : originalFileName;

        Resume resume = new Resume();
        resume.setJobSeekerId(jobSeekerId);
        resume.setTitle(resumeTitle);
        resume.setDefault(isFirstResume);
        resume.setOriginalFileName(originalFileName);
        resume.setStoredFileName(storedFileName);
        resume.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        resume.setFileSize(file.getSize());
        resume.setStoragePath(storagePath);
        resume.setExtractedText(extractedText);
        resume.setParsingStatus(status);
        resume.setSkills(skills);
        resume.setEducation(education);
        resume.setExperience(experience);
        resume.setProjects(projects);
        resume.setCertifications(certifications);
        resume.setSummary(summary);
        resume.setUploadedAt(Instant.now());
        resume.setUpdatedAt(Instant.now());

        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.fromEntity(saved);
    }

    public ResumeResponse uploadResume(String jobSeekerId, MultipartFile file) {
        return uploadResumeVersion(jobSeekerId, null, file);
    }

    public List<ResumeResponse> getAllResumesForJobSeeker(String jobSeekerId) {
        List<Resume> resumes = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
        return resumes.stream()
                .map(ResumeResponse::fromEntity)
                .toList();
    }

    public ResumeResponse getResumeVersionById(String resumeId, String jobSeekerId) {
        Resume resume = resumeRepository.findByIdAndJobSeekerId(resumeId, jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found or access denied."));
        return ResumeResponse.fromEntity(resume);
    }

    public ResumeResponse getResumeByJobSeekerId(String jobSeekerId) {
        Resume resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId).stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No resume found. Please upload a resume first."));
        return ResumeResponse.fromEntity(resume);
    }

    public ResumeResponse setDefaultResume(String resumeId, String jobSeekerId) {
        Resume target = resumeRepository.findByIdAndJobSeekerId(resumeId, jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found or access denied."));

        List<Resume> userResumes = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
        for (Resume r : userResumes) {
            boolean isTarget = r.getId().equals(target.getId());
            if (r.isDefault() != isTarget) {
                r.setDefault(isTarget);
                r.setUpdatedAt(Instant.now());
                resumeRepository.save(r);
            }
        }
        target.setDefault(true);
        return ResumeResponse.fromEntity(target);
    }

    public ResumeResponse updateResumeVersion(String resumeId, String jobSeekerId, String title, MultipartFile file) {
        Resume resume = resumeRepository.findByIdAndJobSeekerId(resumeId, jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found or access denied."));

        if (StringUtils.hasText(title)) {
            resume.setTitle(title.trim());
        }

        if (file != null && !file.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
            }

            String extension = getFileExtension(originalFileName).toLowerCase();
            if (!extension.equals("pdf") && !extension.equals("docx") && !extension.equals("doc")) {
                throw new BadRequestException("Only PDF and DOCX files are supported.");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                throw new BadRequestException("File size exceeds 10MB limit.");
            }

            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read file bytes " + originalFileName, ex);
            }

            String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storagePath;
            try {
                storagePath = fileStorageService.storeFile(new ByteArrayInputStream(bytes), originalFileName, storedFileName);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to store file " + originalFileName, ex);
            }

            // Delete old physical file
            if (resume.getStoragePath() != null) {
                fileStorageService.deleteFile(resume.getStoragePath());
            }

            String extractedText = "";
            ParsingStatus status = ParsingStatus.PENDING;
            try {
                extractedText = extractTextFromStream(new ByteArrayInputStream(bytes), extension);
                status = StringUtils.hasText(extractedText) ? ParsingStatus.SUCCESS : ParsingStatus.FAILED;
            } catch (Exception e) {
                status = ParsingStatus.FAILED;
            }

            resume.setOriginalFileName(originalFileName);
            resume.setStoredFileName(storedFileName);
            resume.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            resume.setFileSize(file.getSize());
            resume.setStoragePath(storagePath);
            resume.setExtractedText(extractedText);
            resume.setParsingStatus(status);
            resume.setSkills(extractSkills(extractedText));
            resume.setEducation(extractEducation(extractedText));
            resume.setExperience(extractExperience(extractedText));
            resume.setProjects(extractProjects(extractedText));
            resume.setCertifications(extractCertifications(extractedText));
            resume.setSummary(extractSummary(extractedText));
        }

        resume.setUpdatedAt(Instant.now());
        Resume saved = resumeRepository.save(resume);
        return ResumeResponse.fromEntity(saved);
    }

    public void deleteResumeVersion(String resumeId, String jobSeekerId) {
        Resume resume = resumeRepository.findByIdAndJobSeekerId(resumeId, jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found or access denied."));

        boolean wasDefault = resume.isDefault();

        if (resume.getStoragePath() != null) {
            fileStorageService.deleteFile(resume.getStoragePath());
        }
        resumeRepository.delete(resume);

        // If the deleted resume was default, assign a new default among remaining resumes
        if (wasDefault) {
            List<Resume> remaining = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
            if (!remaining.isEmpty()) {
                Resume newDefault = remaining.get(0);
                newDefault.setDefault(true);
                newDefault.setUpdatedAt(Instant.now());
                resumeRepository.save(newDefault);
            }
        }
    }

    public void deleteResume(String jobSeekerId) {
        List<Resume> resumes = resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId);
        if (resumes.isEmpty()) {
            throw new ResourceNotFoundException("No resume found to delete.");
        }
        for (Resume r : resumes) {
            if (r.getStoragePath() != null) {
                fileStorageService.deleteFile(r.getStoragePath());
            }
        }
        resumeRepository.deleteAll(resumes);
    }

    public Resume getResumeEntityById(String resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));
    }

    public Resource loadResumeFile(String jobSeekerId) {
        Resume resume = resumeRepository.findByJobSeekerIdAndIsDefaultTrue(jobSeekerId)
                .or(() -> resumeRepository.findByJobSeekerIdOrderByUploadedAtDesc(jobSeekerId).stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found for job seeker"));

        return fileStorageService.loadFileAsResource(resume.getStoragePath());
    }

    public Resource loadResumeFileById(String resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        return fileStorageService.loadFileAsResource(resume.getStoragePath());
    }

    public Resource loadResumeFileByIdAndJobSeekerId(String resumeId, String jobSeekerId) {
        Resume resume = resumeRepository.findByIdAndJobSeekerId(resumeId, jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found or access denied."));

        return fileStorageService.loadFileAsResource(resume.getStoragePath());
    }

    private String extractTextFromStream(InputStream inputStream, String extension) throws Exception {
        if ("pdf".equalsIgnoreCase(extension)) {
            try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } else if ("docx".equalsIgnoreCase(extension) || "doc".equalsIgnoreCase(extension)) {
            try (XWPFDocument doc = new XWPFDocument(inputStream);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                return extractor.getText();
            }
        }
        return "";
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    // Basic rule-based extraction helpers
    private List<String> extractSkills(String text) {
        if (!StringUtils.hasText(text)) return Collections.emptyList();
        List<String> matched = new ArrayList<>();
        String lowerText = text.toLowerCase();
        for (String skill : COMMON_SKILLS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill.toLowerCase()) + "\\b");
            if (pattern.matcher(lowerText).find()) {
                matched.add(skill);
            }
        }
        return matched;
    }

    private List<String> extractEducation(String text) {
        if (!StringUtils.hasText(text)) return Collections.emptyList();
        List<String> eduList = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.contains("bachelor") || lower.contains("master") || lower.contains("degree")
                    || lower.contains("b.tech") || lower.contains("m.tech") || lower.contains("b.s.")
                    || lower.contains("m.s.") || lower.contains("university") || lower.contains("college")
                    || lower.contains("institute")) {
                if (trimmed.length() <= 120 && !trimmed.isEmpty()) {
                    eduList.add(trimmed);
                }
            }
        }
        return eduList.size() > 5 ? eduList.subList(0, 5) : eduList;
    }

    private List<String> extractExperience(String text) {
        if (!StringUtils.hasText(text)) return Collections.emptyList();
        List<String> expList = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.contains("developer") || lower.contains("engineer") || lower.contains("architect")
                    || lower.contains("intern") || lower.contains("analyst") || lower.contains("lead")
                    || lower.contains("manager") || lower.contains("specialist")) {
                if (trimmed.length() <= 120 && !trimmed.isEmpty()) {
                    expList.add(trimmed);
                }
            }
        }
        return expList.size() > 5 ? expList.subList(0, 5) : expList;
    }

    private List<String> extractProjects(String text) {
        if (!StringUtils.hasText(text)) return Collections.emptyList();
        List<String> projects = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        boolean inProjectSection = false;
        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.contains("project") || lower.contains("portfolio")) {
                inProjectSection = true;
                continue;
            }
            if (inProjectSection) {
                if (trimmed.isEmpty()) continue;
                if (lower.contains("education") || lower.contains("experience") || lower.contains("skills")) {
                    break;
                }
                if (trimmed.length() <= 120) {
                    projects.add(trimmed);
                }
            }
        }
        return projects.size() > 5 ? projects.subList(0, 5) : projects;
    }

    private List<String> extractCertifications(String text) {
        if (!StringUtils.hasText(text)) return Collections.emptyList();
        List<String> certs = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            String lower = trimmed.toLowerCase();
            if (lower.contains("certified") || lower.contains("certificate") || lower.contains("certification")) {
                if (trimmed.length() <= 120 && !trimmed.isEmpty()) {
                    certs.add(trimmed);
                }
            }
        }
        return certs.size() > 5 ? certs.subList(0, 5) : certs;
    }

    private String extractSummary(String text) {
        if (!StringUtils.hasText(text)) return "";
        String[] lines = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                sb.append(trimmed).append(" ");
                count++;
                if (count >= 3) break;
            }
        }
        return sb.toString().trim();
    }
}
