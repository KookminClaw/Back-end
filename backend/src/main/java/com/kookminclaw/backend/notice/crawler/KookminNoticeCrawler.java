package com.kookminclaw.backend.notice.crawler;

import com.kookminclaw.backend.notice.crawler.dto.CrawledNotice;
import com.kookminclaw.backend.notice.crawler.dto.NoticeCrawlResult;
import com.kookminclaw.backend.notice.domain.NoticeCategory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class KookminNoticeCrawler {

    private static final Logger log = LoggerFactory.getLogger(KookminNoticeCrawler.class);

    private static final String LIST_URL = "https://www.kookmin.ac.kr/user/kmuNews/notice/index.do";
    private static final String SOURCE = "국민대학교";
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; KookminClawBot/1.0)";
    private static final int TIMEOUT_MILLIS = 10_000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    public List<CrawledNotice> crawl() {
        return crawlWithResult().notices();
    }

    public NoticeCrawlResult crawlWithResult() {
        List<CrawledNotice> notices = new ArrayList<>();
        int failedCount = 0;

        try {
            Document listDocument = fetch(LIST_URL);

            // TODO: 국민대학교 대표 홈페이지의 공지 목록 HTML 구조 변경 시 selector 재검증 필요.
            Elements noticeLinks = listDocument.select(".board_list > ul > li > a[href]");

            for (Element noticeLink : noticeLinks) {
                try {
                    parseNotice(noticeLink).ifPresent(notices::add);
                } catch (Exception e) {
                    failedCount++;
                    log.warn(
                            "Failed to crawl Kookmin notice item. url={}, reason={}",
                            noticeLink.absUrl("href"),
                            e.getMessage(),
                            e
                    );
                }
            }
        } catch (IOException e) {
            failedCount++;
            log.warn("Failed to crawl Kookmin notice list. url={}, reason={}", LIST_URL, e.getMessage(), e);
        }

        return new NoticeCrawlResult(notices, failedCount);
    }

    private java.util.Optional<CrawledNotice> parseNotice(Element noticeLink) throws IOException {
        String sourceUrl = noticeLink.absUrl("href");
        if (sourceUrl == null || sourceUrl.isBlank()) {
            log.warn("Skip Kookmin notice because detail url is empty.");
            return java.util.Optional.empty();
        }

        Element boardText = noticeLink.selectFirst(".board_txt");
        if (boardText == null) {
            log.warn("Skip Kookmin notice because board text area is missing. url={}", sourceUrl);
            return java.util.Optional.empty();
        }

        String title = textOf(boardText, ".title");
        if (title.isBlank()) {
            log.warn("Skip Kookmin notice because title is empty. url={}", sourceUrl);
            return java.util.Optional.empty();
        }

        String categoryText = textOf(boardText, ".ctg_name");
        OffsetDateTime publishedAt = parsePublishedAt(boardText.select(".board_etc span").first());
        String content = crawlContent(sourceUrl);

        return java.util.Optional.of(new CrawledNotice(
                title,
                content,
                sourceUrl,
                SOURCE,
                toNoticeCategory(categoryText),
                publishedAt
        ));
    }

    private String crawlContent(String sourceUrl) throws IOException {
        Document detailDocument = fetch(sourceUrl);

        // TODO: 상세 본문 컨테이너 selector는 현재 국민대학교 HTML 구조 기준이다.
        Element contentElement = detailDocument.selectFirst(".board_view .view_cont .view_inner");
        if (contentElement == null) {
            log.warn("Kookmin notice content area is missing. url={}", sourceUrl);
            return "";
        }

        return contentElement.text().trim();
    }

    private Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MILLIS)
                .get();
    }

    private String textOf(Element element, String selector) {
        Element selected = element.selectFirst(selector);
        return selected != null ? selected.text().trim() : "";
    }

    private OffsetDateTime parsePublishedAt(Element dateElement) {
        if (dateElement == null) {
            return null;
        }

        String dateText = dateElement.text().trim();
        try {
            return LocalDate.parse(dateText, DATE_FORMATTER)
                    .atStartOfDay(SEOUL_ZONE)
                    .toOffsetDateTime();
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse Kookmin notice published date. date={}", dateText, e);
            return null;
        }
    }

    private NoticeCategory toNoticeCategory(String categoryText) {
        return switch (categoryText) {
            case "학사공지" -> NoticeCategory.학사;
            case "장학공지" -> NoticeCategory.장학;
            case "교내채용", "교외채용" -> NoticeCategory.취업;
            case "특강공지", "공모∙행사", "행사·활동·이벤트 공지" -> NoticeCategory.행사;
            default -> NoticeCategory.기타;
        };
    }
}
