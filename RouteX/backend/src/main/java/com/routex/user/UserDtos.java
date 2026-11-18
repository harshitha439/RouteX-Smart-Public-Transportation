package com.routex.user;

import jakarta.validation.constraints.*;

public final class UserDtos {
    private UserDtos() {}

    public record ProfileResponse(Long id, String fullName, String email, String phone, Role role,
                                  boolean active, String emergencyContactName, String emergencyContactPhone) {
        public static ProfileResponse from(User u) {
            return new ProfileResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(),
                    u.isActive(), u.getEmergencyContactName(), u.getEmergencyContactPhone());
        }
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 100) String fullName,
            @Pattern(regexp = "^$|^[0-9+\\- ]{7,20}$", message = "Invalid phone number") String phone,
            @Size(max = 100) String emergencyContactName,
            @Pattern(regexp = "^$|^[0-9+\\- ]{7,20}$", message = "Invalid phone number") String emergencyContactPhone) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String newPassword) {}
}
