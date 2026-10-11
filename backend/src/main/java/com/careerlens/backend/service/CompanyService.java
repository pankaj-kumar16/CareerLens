package com.careerlens.backend.service;

import com.careerlens.backend.dto.CompanyResponse;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Page<CompanyResponse> listCompanies(Pageable pageable) {
        return companyRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(CompanyResponse::from);
    }

    public CompanyResponse getCompany(String id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found."));
        return CompanyResponse.from(company);
    }

    public CompanyResponse createCompany(String userEmail, Company company) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (companyRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("Company profile already exists for this account.");
        }

        company.setUserId(user.getId());
        company.setCreatedAt(Instant.now());
        company.setUpdatedAt(Instant.now());

        Company saved = companyRepository.save(company);
        return CompanyResponse.from(saved);
    }

    public CompanyResponse getMyCompany(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Company company = companyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company profile not found."));
        return CompanyResponse.from(company);
    }

    public CompanyResponse updateMyCompany(String userEmail, Company updates) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Company company = companyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Company profile not found."));

        if (updates.getName() != null) company.setName(updates.getName());
        if (updates.getLogo() != null) company.setLogo(updates.getLogo());
        if (updates.getDescription() != null) company.setDescription(updates.getDescription());
        if (updates.getIndustry() != null) company.setIndustry(updates.getIndustry());
        if (updates.getLocation() != null) company.setLocation(updates.getLocation());
        if (updates.getWebsite() != null) company.setWebsite(updates.getWebsite());
        if (updates.getCompanySize() != null) company.setCompanySize(updates.getCompanySize());
        if (updates.getFoundedYear() != null) company.setFoundedYear(updates.getFoundedYear());

        company.setUpdatedAt(Instant.now());
        Company saved = companyRepository.save(company);
        return CompanyResponse.from(saved);
    }
}
