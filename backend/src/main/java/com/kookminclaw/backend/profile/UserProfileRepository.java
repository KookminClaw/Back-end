package com.kookminclaw.backend.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    boolean existsByStudentNumber(String studentNumber);
}
