package org.example.config;


import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
@DeclareRoles({"USER", "PREMIUM"})
public class JaxRsActivator extends Application {
}