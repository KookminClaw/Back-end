package com.kookminclaw.backend.notice.controller;

import com.kookminclaw.backend.notice.dto.NoticeCreateRequest;
import com.kookminclaw.backend.notice.dto.NoticeDetailResponse;
import com.kookminclaw.backend.notice.dto.NoticeResponse;
import com.kookminclaw.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public List<NoticeResponse> getNotices() {
        return noticeService.getNotices();
    }

    @GetMapping("/{id}")
    public NoticeDetailResponse getNoticeDetail(@PathVariable Long id) {
        return noticeService.getNoticeDetail(id);
    }

    @PostMapping
    public Long createNotice(@RequestBody NoticeCreateRequest request) {
        return noticeService.createNotice(request);
    }
}