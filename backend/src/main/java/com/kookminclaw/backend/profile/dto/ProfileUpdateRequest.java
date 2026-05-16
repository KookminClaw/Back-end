package com.kookminclaw.backend.profile.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record ProfileUpdateRequest(

        @Min(1) @Max(5)
        Short grade,

        String departmentCode,

        @Pattern(regexp = "enrolled|leave|graduated")
        String enrollmentStatus,

        @Size(max = 10)
        List<String> interestKeywords,

        @Size(max = 5)
        List<String> careerGoals,

        @Size(max = 10)
        List<String> courseInterests,

        @Size(max = 10)
        List<String> extracurricularInterests,

        Boolean scholarshipInterest,

        Boolean notifyPush,

        Boolean notifyEmail,

        @Size(max = 10)
        List<String> notifyCategories
) {}
