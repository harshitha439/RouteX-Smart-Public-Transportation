package com.routex.user;

import com.routex.user.UserDtos.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @GetMapping("/me")
    public ProfileResponse me(Authentication auth) {
        return ProfileResponse.from(service.getByEmail(auth.getName()));
    }

    @PutMapping("/me")
    public ProfileResponse update(Authentication auth, @Valid @RequestBody UpdateProfileRequest req) {
        return ProfileResponse.from(service.updateProfile(auth.getName(), req));
    }

    @PutMapping("/me/password")
    public Map<String, String> changePassword(Authentication auth, @Valid @RequestBody ChangePasswordRequest req) {
        service.changePassword(auth.getName(), req);
        return Map.of("message", "Password changed successfully");
    }
}
