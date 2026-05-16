package com.kookminclaw.backend.profile.dto;

import com.kookminclaw.backend.profile.UserProfile;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String studentNumber,
        Short grade,
        String departmentCode,
        String enrollmentStatus,
        List<String> interestKeywords,
        List<String> careerGoals,
        List<String> courseInterests,
        List<String> extracurricularInterests,
        Boolean scholarshipInterest,
        boolean notifyPush,
        boolean notifyEmail,
        List<String> notifyCategories,
        Short profileCompletionRate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProfileResponse from(UserProfile p) {
        return new ProfileResponse(
                p.getUserId(),
                p.getStudentNumber(),
                p.getGrade(),
                p.getDepartmentCode(),
                p.getEnrollmentStatus(),
                toList(p.getInterestKeywords()),
                toList(p.getCareerGoals()),
                toList(p.getCourseInterests()),
                toList(p.getExtracurricularInterests()),
                p.getScholarshipInterest(),
                p.isNotifyPush(),
                p.isNotifyEmail(),
                toList(p.getNotifyCategories()),
                p.getProfileCompletionRate(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private static List<String> toList(String[] arr) {
        return arr == null ? List.of() : Arrays.asList(arr);
    }
}
