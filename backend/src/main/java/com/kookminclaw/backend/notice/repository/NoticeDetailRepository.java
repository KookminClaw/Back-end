package com.kookminclaw.backend.notice.repository;

import com.kookminclaw.backend.notice.domain.NoticeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeDetailRepository extends JpaRepository<NoticeDetail, Long> {

    Optional<NoticeDetail> findByNoticeId(Long noticeId);
}