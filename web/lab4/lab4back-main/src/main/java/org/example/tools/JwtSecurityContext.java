package org.example.tools;

import jakarta.ws.rs.core.SecurityContext;
import java.security.Principal;

public class JwtSecurityContext implements SecurityContext, Principal, JwtSecurityContextInterface {
    private final Long userId;
    private final String email;
    private final String role;

    public JwtSecurityContext(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.role != null && this.role.equals(role);
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Principal getUserPrincipal() {
        return this;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}