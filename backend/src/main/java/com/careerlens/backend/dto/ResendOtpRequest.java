package com.careerlens.backend.dto;

import com.careerlens.backend.model.OtpType;
import com.careerlens.backend.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private Role role;
    private OtpType type;

    public ResendOtpRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public OtpType getType() { return type; }
    public void setType(OtpType type) { this.type = type; }
}
