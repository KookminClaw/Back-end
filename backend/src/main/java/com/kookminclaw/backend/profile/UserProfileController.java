package com.kookminclaw.backend.profile;

import com.kookminclaw.backend.profile.dto.ProfileCreateRequest;
import com.kookminclaw.backend.profile.dto.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Profile", description = "사용자 프로필 API")
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService service;

    @Operation(summary = "프로필 생성", description = "학번 기준으로 프로필을 생성합니다. 학번 중복 시 409 반환.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로필 생성 성공"),
            @ApiResponse(responseCode = "409", description = "학번 중복")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@Valid @RequestBody ProfileCreateRequest request) {
        return service.create(request);
    }

    @Operation(summary = "프로필 조회", description = "userId로 프로필을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필 없음")
    })
    @GetMapping("/{userId}")
    public ProfileResponse get(@PathVariable UUID userId) {
        return service.get(userId);
    }
}
