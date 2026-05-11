package com.kookminclaw.backend.profile;

import com.kookminclaw.backend.profile.dto.ProfileCreateRequest;
import com.kookminclaw.backend.profile.dto.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    @Transactional
    public ProfileResponse create(ProfileCreateRequest req) {
        if (repository.existsByStudentNumber(req.studentNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 학번입니다.");
        }

        UserProfile profile = UserProfile.builder()
                .studentNumber(req.studentNumber())
                .grade(req.grade())
                .departmentCode(req.departmentCode())
                .enrollmentStatus(req.enrollmentStatus() != null ? req.enrollmentStatus() : "enrolled")
                .interestKeywords(toArray(req.interestKeywords()))
                .careerGoals(toArray(req.careerGoals()))
                .courseInterests(toArray(req.courseInterests()))
                .extracurricularInterests(toArray(req.extracurricularInterests()))
                .scholarshipInterest(req.scholarshipInterest() != null ? req.scholarshipInterest() : true)
                .notifyPush(req.notifyPush())
                .notifyEmail(req.notifyEmail())
                .notifyCategories(toArray(req.notifyCategories()))
                .build();

        return ProfileResponse.from(repository.save(profile));
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(UUID userId) {
        return repository.findById(userId)
                .map(ProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."));
    }

    private String[] toArray(List<String> list) {
        return list == null ? null : list.toArray(String[]::new);
    }
}
