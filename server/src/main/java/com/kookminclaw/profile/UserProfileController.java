package com.kookminclaw.profile;

import com.kookminclaw.profile.dto.ProfileCreateRequest;
import com.kookminclaw.profile.dto.ProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@Valid @RequestBody ProfileCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/{userId}")
    public ProfileResponse get(@PathVariable UUID userId) {
        return service.get(userId);
    }
}
