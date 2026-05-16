package com.kookminclaw.backend.notice.crawler;

import com.kookminclaw.backend.notice.crawler.dto.NoticeCollectResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticeCollectScheduler {

    private static final Logger log = LoggerFactory.getLogger(NoticeCollectScheduler.class);

    private final NoticeCollectService noticeCollectService;

    @Scheduled(cron = "${notice.collect.cron:0 0 * * * *}")
    public void collectNotices() {
        log.info("Scheduled notice collection started.");

        try {
            NoticeCollectResult result = noticeCollectService.collect();
            log.info(
                    "Scheduled notice collection completed. savedCount={}, duplicateCount={}, failedCount={}",
                    result.savedCount(),
                    result.duplicateCount(),
                    result.failedCount()
            );
        } catch (Exception e) {
            log.error("Scheduled notice collection failed.", e);
        }
    }
}
