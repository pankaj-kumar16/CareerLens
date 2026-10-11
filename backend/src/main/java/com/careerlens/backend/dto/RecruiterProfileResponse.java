package com.careerlens.backend.dto;

public class RecruiterProfileResponse {

    private String userId;
    private String fullName;
    private String phone;
    private String position;
    private String companyId;

    public RecruiterProfileResponse() {}

    public static RecruiterProfileResponse from(com.careerlens.backend.model.RecruiterProfile profile) {
        RecruiterProfileResponse response = new RecruiterProfileResponse();
        response.setUserId(profile.getUserId());
        response.setFullName(profile.getFullName());
        response.setPhone(profile.getPhone());
        response.setPosition(profile.getPosition());
        response.setCompanyId(profile.getCompanyId());
        return response;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
}
