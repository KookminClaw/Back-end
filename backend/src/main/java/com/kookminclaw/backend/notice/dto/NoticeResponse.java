package com.kookminclaw.backend.notice.dto;

import com.kookminclaw.backend.notice.domain.NoticeCategory;

import java.time.OffsetDateTime;

public record NoticeResponse(
        Long id,
        String title,
        String link,
        OffsetDateTime published,
        String source,
        NoticeCategory category,
        Short importance,
        OffsetDateTime deadline,
        String targetGrade
) {
}