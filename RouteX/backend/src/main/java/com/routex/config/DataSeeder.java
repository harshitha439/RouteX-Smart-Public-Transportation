package com.routex.config;

import com.routex.user.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final UserService users;
    private final UserRepository repo;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(UserService users, UserRepository repo,
                      @Value("${routex.admin.email:}") String adminEmail,
                      @Value("${routex.admin.password:}") String adminPassword) {
        this.users = users; this.repo = repo;
        this.adminEmail = adminEmail; this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!adminEmail.isBlank() && !adminPassword.isBlank() && !repo.existsByEmail(adminEmail.toLowerCase())) {
            users.create("RouteX Admin", adminEmail, null, adminPassword, Role.ADMIN);
            System.out.println("Seeded admin: " + adminEmail);
        }
    }
}
