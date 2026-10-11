package com.careerlens.backend.dto;

import com.careerlens.backend.model.Company;

public class CompanyResponse {

    private String id;
    private String name;
    private String logo;
    private String description;
    private String industry;
    private String location;
    private String website;
    private String companySize;
    private Integer foundedYear;

    public CompanyResponse() {}

    public static CompanyResponse from(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setLogo(company.getLogo());
        response.setDescription(company.getDescription());
        response.setIndustry(company.getIndustry());
        response.setLocation(company.getLocation());
        response.setWebsite(company.getWebsite());
        response.setCompanySize(company.getCompanySize());
        response.setFoundedYear(company.getFoundedYear());
        return response;
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
}
