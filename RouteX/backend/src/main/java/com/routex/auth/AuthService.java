package com.routex.auth;

import com.routex.auth.AuthDtos.*;
import com.routex.exception.ApiException;
import com.routex.security.JwtService;
import com.routex.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserService userService, UserRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.userService = userService;
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Public registration ALWAYS creates a PASSENGER. Roles can never come from the client. */
    public AuthResponse register(RegisterRequest req) {
        User u = userService.create(req.fullName(), req.email(), req.phone(), req.password(), Role.PASSENGER);
        return toResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        User u = repo.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!u.isActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }
        return toResponse(u);
    }

    private AuthResponse toResponse(User u) {
        return new AuthResponse(jwt.generateToken(u), u.getRole(), u.getFullName(), u.getEmail());
    }
}
