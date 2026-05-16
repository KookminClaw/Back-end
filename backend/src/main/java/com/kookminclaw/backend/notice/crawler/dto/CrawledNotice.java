package com.kookminclaw.backend.notice.crawler.dto;

import com.kookminclaw.backend.notice.domain.NoticeCategory;

import java.time.OffsetDateTime;

public record CrawledNotice(
        String title,
        String content,
        String sourceUrl,
        String source,
        NoticeCategory category,
        OffsetDateTime publishedAt
) {
}
