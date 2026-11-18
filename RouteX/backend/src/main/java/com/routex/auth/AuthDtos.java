package com.routex.auth;

import com.routex.user.Role;
import jakarta.validation.constraints.*;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email @Size(max = 150) String email,
            @Pattern(regexp = "^$|^[0-9+\\- ]{7,20}$", message = "Invalid phone number") String phone,
            @NotBlank @Size(min = 8, max = 72, message = "Password must be 8-72 characters") String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record AuthResponse(String token, Role role, String fullName, String email) {}
}
