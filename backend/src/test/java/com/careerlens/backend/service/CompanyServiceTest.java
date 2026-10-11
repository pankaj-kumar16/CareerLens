package com.careerlens.backend.service;

import com.careerlens.backend.dto.CompanyResponse;
import com.careerlens.backend.model.Company;
import com.careerlens.backend.model.Role;
import com.careerlens.backend.model.User;
import com.careerlens.backend.repository.CompanyRepository;
import com.careerlens.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CompanyServiceTest {

    @MockBean
    private CompanyRepository companyRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private CompanyService companyService;

    @Test
    @DisplayName("Create Company - Recruiter Success")
    void testCreateCompany_Success() {
        User recruiter = new User("rec1", "recruiter@example.com", "Recruiter One", null, Role.RECRUITER, true, true);
        when(userRepository.findByEmail("recruiter@example.com")).thenReturn(Optional.of(recruiter));
        when(companyRepository.findByUserId("rec1")).thenReturn(Optional.empty());

        Company companyInput = new Company();
        companyInput.setName("Tech Corp");
        companyInput.setIndustry("Technology");

        Company savedCompany = new Company();
        savedCompany.setId("comp1");
        savedCompany.setUserId("rec1");
        savedCompany.setName("Tech Corp");
        savedCompany.setIndustry("Technology");

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        CompanyResponse res = companyService.createCompany("recruiter@example.com", companyInput);

        assertNotNull(res);
        assertEquals("comp1", res.getId());
        assertEquals("Tech Corp", res.getName());
    }

    @Test
    @DisplayName("Create Company - Prevent Duplicate Company Creation per Recruiter")
    void testCreateCompany_AlreadyExists() {
        User recruiter = new User("rec1", "recruiter@example.com", "Recruiter One", null, Role.RECRUITER, true, true);
        when(userRepository.findByEmail("recruiter@example.com")).thenReturn(Optional.of(recruiter));
        when(companyRepository.findByUserId("rec1")).thenReturn(Optional.of(new Company()));

        assertThrows(IllegalArgumentException.class, () ->
                companyService.createCompany("recruiter@example.com", new Company())
        );
    }

    @Test
    @DisplayName("Update Own Company - Recruiter Ownership Isolation Check")
    void testUpdateMyCompany_Success() {
        User recruiter = new User("rec1", "recruiter@example.com", "Recruiter One", null, Role.RECRUITER, true, true);
        when(userRepository.findByEmail("recruiter@example.com")).thenReturn(Optional.of(recruiter));

        Company existingCompany = new Company();
        existingCompany.setId("comp1");
        existingCompany.setUserId("rec1");
        existingCompany.setName("Old Tech");

        when(companyRepository.findByUserId("rec1")).thenReturn(Optional.of(existingCompany));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        Company updates = new Company();
        updates.setName("New Tech");

        CompanyResponse res = companyService.updateMyCompany("recruiter@example.com", updates);

        assertEquals("New Tech", res.getName());
    }
}
