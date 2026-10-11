package com.careerlens.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "companies")
public class Company {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String logo;
    private String description;
    private String industry;
    private String location;
    private String website;
    private String companySize;
    private Integer foundedYear;
    private String userId;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Company() {}

    public Company(String id, String name, String logo, String description, String industry, String location,
                   String website, String companySize, Integer foundedYear, String userId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.description = description;
        this.industry = industry;
        this.location = location;
        this.website = website;
        this.companySize = companySize;
        this.foundedYear = foundedYear;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
