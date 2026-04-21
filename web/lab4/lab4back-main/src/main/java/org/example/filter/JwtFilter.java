package org.example.filter;

import jakarta.annotation.Priority;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.example.service.JWTService;
import org.example.tools.JwtSecurityContext;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @EJB
    private JWTService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring("Bearer ".length());

        if (!jwtService.validateToken(token)) {
            requestContext.abortWith(Response.status(401).build());
            return;
        }

        Long userId = jwtService.extractUserId(token);
        String name = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        JwtSecurityContext principal = new JwtSecurityContext(userId, name, role);

        SecurityContext original = requestContext.getSecurityContext();

        SecurityContext newCtx = new SecurityContext() {
            @Override
            public JwtSecurityContext getUserPrincipal() {
                return principal;
            }

            @Override
            public boolean isUserInRole(String role) {
                return principal.isUserInRole(role);
            }

            @Override
            public boolean isSecure() {
                return original != null && original.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        };

        requestContext.setSecurityContext(newCtx);
    }
}
