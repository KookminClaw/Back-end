package com.kookminclaw.backend.profile;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "student_number", nullable = false, unique = true, length = 20)
    private String studentNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "profile_completion_rate")
    private Short profileCompletionRate;

    @Column(name = "grade", nullable = false)
    private Short grade;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @Column(name = "enrollment_status", nullable = false, length = 20)
    private String enrollmentStatus;

    @Type(StringArrayType.class)
    @Column(name = "interest_keywords", columnDefinition = "text[]")
    private String[] interestKeywords;

    @Type(StringArrayType.class)
    @Column(name = "career_goals", columnDefinition = "text[]")
    private String[] careerGoals;

    @Type(StringArrayType.class)
    @Column(name = "course_interests", columnDefinition = "text[]")
    private String[] courseInterests;

    @Type(StringArrayType.class)
    @Column(name = "extracurricular_interests", columnDefinition = "text[]")
    private String[] extracurricularInterests;

    @Column(name = "scholarship_interest")
    private Boolean scholarshipInterest;

    @Column(name = "notify_push", nullable = false)
    private boolean notifyPush;

    @Column(name = "notify_email", nullable = false)
    private boolean notifyEmail;

    @Type(StringArrayType.class)
    @Column(name = "notify_categories", columnDefinition = "text[]")
    private String[] notifyCategories;

    @Column(name = "last_active_at")
    private OffsetDateTime lastActiveAt;

    @PrePersist
    private void prePersist() {
        if (userId == null) userId = UUID.randomUUID();
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        profileCompletionRate = calculateCompletionRate();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = OffsetDateTime.now();
        profileCompletionRate = calculateCompletionRate();
    }

    private short calculateCompletionRate() {
        int filled = 0;
        if (interestKeywords != null && interestKeywords.length > 0) filled++;
        if (careerGoals != null && careerGoals.length > 0) filled++;
        if (courseInterests != null && courseInterests.length > 0) filled++;
        if (extracurricularInterests != null && extracurricularInterests.length > 0) filled++;
        if (scholarshipInterest != null) filled++;
        if (notifyCategories != null && notifyCategories.length > 0) filled++;
        return (short) (filled * 100 / 6);
    }

    public void update(Short grade, String departmentCode, String enrollmentStatus,
                       String[] interestKeywords, String[] careerGoals, String[] courseInterests,
                       String[] extracurricularInterests, Boolean scholarshipInterest,
                       Boolean notifyPush, Boolean notifyEmail, String[] notifyCategories) {
        if (grade != null) this.grade = grade;
        if (departmentCode != null) this.departmentCode = departmentCode;
        if (enrollmentStatus != null) this.enrollmentStatus = enrollmentStatus;
        if (interestKeywords != null) this.interestKeywords = interestKeywords;
        if (careerGoals != null) this.careerGoals = careerGoals;
        if (courseInterests != null) this.courseInterests = courseInterests;
        if (extracurricularInterests != null) this.extracurricularInterests = extracurricularInterests;
        if (scholarshipInterest != null) this.scholarshipInterest = scholarshipInterest;
        if (notifyPush != null) this.notifyPush = notifyPush;
        if (notifyEmail != null) this.notifyEmail = notifyEmail;
        if (notifyCategories != null) this.notifyCategories = notifyCategories;
    }

    @Builder
    public UserProfile(String studentNumber, Short grade, String departmentCode,
                       String enrollmentStatus, String[] interestKeywords, String[] careerGoals,
                       String[] courseInterests, String[] extracurricularInterests,
                       Boolean scholarshipInterest, boolean notifyPush, boolean notifyEmail,
                       String[] notifyCategories) {
        this.studentNumber = studentNumber;
        this.grade = grade;
        this.departmentCode = departmentCode;
        this.enrollmentStatus = enrollmentStatus;
        this.interestKeywords = interestKeywords;
        this.careerGoals = careerGoals;
        this.courseInterests = courseInterests;
        this.extracurricularInterests = extracurricularInterests;
        this.scholarshipInterest = scholarshipInterest;
        this.notifyPush = notifyPush;
        this.notifyEmail = notifyEmail;
        this.notifyCategories = notifyCategories;
    }
}
