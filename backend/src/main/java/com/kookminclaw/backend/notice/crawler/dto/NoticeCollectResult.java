package com.kookminclaw.backend.notice.crawler.dto;

public record NoticeCollectResult(
        int savedCount,
        int duplicateCount,
        int failedCount
) {
}
