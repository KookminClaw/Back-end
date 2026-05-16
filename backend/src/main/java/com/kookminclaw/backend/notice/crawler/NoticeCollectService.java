package com.kookminclaw.backend.notice.crawler;

import com.kookminclaw.backend.notice.crawler.dto.CrawledNotice;
import com.kookminclaw.backend.notice.crawler.dto.NoticeCrawlResult;
import com.kookminclaw.backend.notice.crawler.dto.NoticeCollectResult;
import com.kookminclaw.backend.notice.domain.Notice;
import com.kookminclaw.backend.notice.domain.NoticeCategory;
import com.kookminclaw.backend.notice.domain.NoticeDetail;
import com.kookminclaw.backend.notice.repository.NoticeDetailRepository;
import com.kookminclaw.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeCollectService {

    private static final Logger log = LoggerFactory.getLogger(NoticeCollectService.class);

    private final KookminNoticeCrawler kookminNoticeCrawler;
    private final NoticeRepository noticeRepository;
    private final NoticeDetailRepository noticeDetailRepository;

    @Transactional
    public NoticeCollectResult collect() {
        int savedCount = 0;
        int duplicateCount = 0;
        NoticeCrawlResult crawlResult = kookminNoticeCrawler.crawlWithResult();
        int failedCount = crawlResult.failedCount();

        List<CrawledNotice> crawledNotices = crawlResult.notices();

        for (CrawledNotice crawledNotice : crawledNotices) {
            try {
                if (crawledNotice.sourceUrl() == null || crawledNotice.sourceUrl().isBlank()) {
                    failedCount++;
                    log.warn("Skip notice collection because sourceUrl is empty. title={}", crawledNotice.title());
                    continue;
                }

                if (noticeRepository.existsBySourceUrl(crawledNotice.sourceUrl())) {
                    duplicateCount++;
                    continue;
                }

                saveNotice(crawledNotice);
                savedCount++;
            } catch (Exception e) {
                failedCount++;
                log.warn("Failed to save crawled notice. sourceUrl={}, title={}",
                        crawledNotice.sourceUrl(),
                        crawledNotice.title(),
                        e);
            }
        }

        log.info("Notice collection finished. savedCount={}, duplicateCount={}, failedCount={}",
                savedCount,
                duplicateCount,
                failedCount);

        return new NoticeCollectResult(savedCount, duplicateCount, failedCount);
    }

    private void saveNotice(CrawledNotice crawledNotice) {
        Notice notice = Notice.builder()
                .title(crawledNotice.title())
                .link(crawledNotice.sourceUrl())
                .published(resolvePublishedAt(crawledNotice))
                .source(crawledNotice.source())
                .category(resolveCategory(crawledNotice))
                .importance((short) 0)
                .deadline(null)
                .targetGrade(null)
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        NoticeDetail detail = NoticeDetail.builder()
                .notice(savedNotice)
                .body(crawledNotice.content())
                .attachments(null)
                .summary(null)
                .build();

        noticeDetailRepository.save(detail);
    }

    private OffsetDateTime resolvePublishedAt(CrawledNotice crawledNotice) {
        return crawledNotice.publishedAt() != null
                ? crawledNotice.publishedAt()
                : OffsetDateTime.now();
    }

    private NoticeCategory resolveCategory(CrawledNotice crawledNotice) {
        return crawledNotice.category() != null
                ? crawledNotice.category()
                : NoticeCategory.기타;
    }
}
