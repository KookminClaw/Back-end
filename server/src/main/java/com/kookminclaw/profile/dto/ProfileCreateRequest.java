package com.kookminclaw.profile.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record ProfileCreateRequest(

        @NotBlank
        String studentNumber,

        @NotNull @Min(1) @Max(5)
        Short grade,

        @NotBlank
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

        boolean notifyPush,

        boolean notifyEmail,

        @Size(max = 10)
        List<String> notifyCategories
) {}
