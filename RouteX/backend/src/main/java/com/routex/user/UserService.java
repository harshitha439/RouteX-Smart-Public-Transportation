package com.routex.user;

import com.routex.exception.ApiException;
import com.routex.user.UserDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User getByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User create(String fullName, String email, String phone, String rawPassword, Role role) {
        String normalized = email.trim().toLowerCase();
        if (repo.existsByEmail(normalized)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setFullName(fullName.trim());
        u.setEmail(normalized);
        u.setPhone(phone);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(role);
        return repo.save(u);
    }

    @Transactional
    public User updateProfile(String email, UpdateProfileRequest req) {
        User u = getByEmail(email);
        u.setFullName(req.fullName().trim());
        u.setPhone(req.phone());
        u.setEmergencyContactName(req.emergencyContactName());
        u.setEmergencyContactPhone(req.emergencyContactPhone());
        return u;
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User u = getByEmail(email);
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
    }
}
