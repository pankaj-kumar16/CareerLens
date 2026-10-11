package com.careerlens.backend.dto;

import com.careerlens.backend.model.Role;
import com.careerlens.backend.model.User;

public class UserResponse {

    private String id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private boolean emailVerified;

    public UserResponse() {}

    public UserResponse(String id, String email, String fullName, String phone, Role role, boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.emailVerified = emailVerified;
    }

    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.isEmailVerified()
        );
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
}
