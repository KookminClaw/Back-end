package com.kookminclaw.backend.notice.crawler.dto;

import java.util.List;

public record NoticeCrawlResult(
        List<CrawledNotice> notices,
        int failedCount
) {
}
