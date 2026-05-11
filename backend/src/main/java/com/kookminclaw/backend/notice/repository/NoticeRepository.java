package com.kookminclaw.backend.notice.repository;

import com.kookminclaw.backend.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}