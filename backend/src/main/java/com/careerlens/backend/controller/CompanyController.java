package com.careerlens.backend.controller;

import com.careerlens.backend.dto.CompanyResponse;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> listCompanies(Pageable pageable) {
        return ResponseEntity.ok(companyService.listCompanies(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable String id) {
        return ResponseEntity.ok(companyService.getCompany(id));
    }
}
