package com.careerlens.backend.controller;

import com.careerlens.backend.dto.CompanyResponse;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter/company")
public class RecruiterCompanyController {

    private final CompanyService companyService;

    public RecruiterCompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(Authentication authentication,
                                                         @Valid @RequestBody Company company) {
        CompanyResponse response = companyService.createCompany(authentication.getName(), company);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CompanyResponse> getMyCompany(Authentication authentication) {
        return ResponseEntity.ok(companyService.getMyCompany(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<CompanyResponse> updateMyCompany(Authentication authentication,
                                                           @Valid @RequestBody Company updates) {
        return ResponseEntity.ok(companyService.updateMyCompany(authentication.getName(), updates));
    }
}
