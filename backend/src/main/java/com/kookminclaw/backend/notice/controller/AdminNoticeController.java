package com.kookminclaw.backend.notice.controller;

import com.kookminclaw.backend.notice.crawler.NoticeCollectService;
import com.kookminclaw.backend.notice.crawler.dto.NoticeCollectResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeCollectService noticeCollectService;

    // TODO: 운영 환경에서는 관리자 인증/권한 검증을 적용해야 한다.
    @PostMapping("/collect")
    public NoticeCollectResult collectNotices() {
        return noticeCollectService.collect();
    }
}
