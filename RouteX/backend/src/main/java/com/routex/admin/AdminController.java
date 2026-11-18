package com.routex.admin;

import com.routex.auth.AuthDtos.RegisterRequest;
import com.routex.exception.ApiException;
import com.routex.user.*;
import com.routex.user.UserDtos.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService service;
    private final UserRepository repo;

    public AdminController(UserService service, UserRepository repo) {
        this.service = service; this.repo = repo;
    }

    @PostMapping("/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createDriver(@Valid @RequestBody RegisterRequest req) {
        return ProfileResponse.from(service.create(req.fullName(), req.email(), req.phone(), req.password(), Role.DRIVER));
    }

    @GetMapping("/users")
    public List<ProfileResponse> list(@RequestParam(required = false) Role role) {
        return (role == null ? repo.findAll() : repo.findByRole(role)).stream().map(ProfileResponse::from).toList();
    }

    @PatchMapping("/users/{id}/active")
    public ProfileResponse setActive(@PathVariable Long id, @RequestParam boolean value) {
        User u = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setActive(value);
        return ProfileResponse.from(repo.save(u));
    }
}
