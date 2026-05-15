package com.kookminclaw.backend.notice.controller;

import com.kookminclaw.backend.notice.dto.NoticeCreateRequest;
import com.kookminclaw.backend.notice.dto.NoticeDetailResponse;
import com.kookminclaw.backend.notice.dto.NoticeResponse;
import com.kookminclaw.backend.notice.dto.NoticeUpdateRequest;
import com.kookminclaw.backend.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 목록 조회", description = "전체 공지 목록을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public List<NoticeResponse> getNotices() {
        return noticeService.getNotices();
    }

    @Operation(summary = "공지 상세 조회", description = "공지 id로 본문 및 AI 요약을 포함한 상세 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공지 없음")
    })
    @GetMapping("/{id}")
    public NoticeDetailResponse getNoticeDetail(@PathVariable Long id) {
        return noticeService.getNoticeDetail(id);
    }

    @Operation(summary = "공지 등록", description = "AI 크롤러가 수집한 공지를 등록합니다. 공지 본문/요약 포함.")
    @ApiResponse(responseCode = "201", description = "등록 성공 — 생성된 공지 id 반환")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createNotice(@RequestBody NoticeCreateRequest request) {
        return noticeService.createNotice(request);
    }

    @Operation(summary = "공지 수정", description = "공지 id로 공지 내용을 수정합니다. 전달한 필드만 업데이트됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "공지 없음")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateNotice(@PathVariable Long id, @RequestBody NoticeUpdateRequest request) {
        noticeService.updateNotice(id, request);
    }

    @Operation(summary = "공지 삭제", description = "공지 id로 공지를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "공지 없음")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }
}