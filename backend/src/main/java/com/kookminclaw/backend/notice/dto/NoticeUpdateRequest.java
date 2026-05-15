package com.kookminclaw.backend.notice.dto;

import com.kookminclaw.backend.notice.domain.NoticeCategory;

import java.time.OffsetDateTime;

public record NoticeUpdateRequest(
        String title,
        String link,
        OffsetDateTime published,
        String source,
        NoticeCategory category,
        Short importance,
        OffsetDateTime deadline,
        String targetGrade,
        String body,
        String attachments,
        String summary
) {
}
