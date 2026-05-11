package com.kookminclaw.backend.notice.service;

import com.kookminclaw.backend.notice.domain.Notice;
import com.kookminclaw.backend.notice.domain.NoticeCategory;
import com.kookminclaw.backend.notice.domain.NoticeDetail;
import com.kookminclaw.backend.notice.dto.NoticeCreateRequest;
import com.kookminclaw.backend.notice.dto.NoticeDetailResponse;
import com.kookminclaw.backend.notice.dto.NoticeResponse;
import com.kookminclaw.backend.notice.repository.NoticeDetailRepository;
import com.kookminclaw.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeDetailRepository noticeDetailRepository;

    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAll()
                .stream()
                .map(notice -> new NoticeResponse(
                        notice.getId(),
                        notice.getTitle(),
                        notice.getLink(),
                        notice.getPublished(),
                        notice.getSource(),
                        notice.getCategory(),
                        notice.getImportance(),
                        notice.getDeadline(),
                        notice.getTargetGrade()
                ))
                .toList();
    }

    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지 없음"));

        NoticeDetail detail = noticeDetailRepository.findByNoticeId(id)
                .orElse(null);

        return new NoticeDetailResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getLink(),
                notice.getPublished(),
                notice.getSource(),
                notice.getCategory(),
                notice.getImportance(),
                notice.getDeadline(),
                notice.getTargetGrade(),
                detail != null ? detail.getBody() : null,
                detail != null ? detail.getAttachments() : null,
                detail != null ? detail.getSummary() : null
        );
    }

    public Long createNotice(NoticeCreateRequest request) {
        NoticeCategory category = request.category() != null
                ? request.category()
                : NoticeCategory.기타;

        Short importance = request.importance() != null
                ? request.importance()
                : 0;

        Notice notice = Notice.builder()
                .title(request.title())
                .link(request.link())
                .published(request.published())
                .source(request.source())
                .category(category)
                .importance(importance)
                .deadline(request.deadline())
                .targetGrade(request.targetGrade())
                .build();

        Notice savedNotice = noticeRepository.save(notice);

        NoticeDetail detail = NoticeDetail.builder()
                .notice(savedNotice)
                .body(request.body())
                .attachments(request.attachments())
                .summary(request.summary())
                .build();

        noticeDetailRepository.save(detail);

        return savedNotice.getId();
    }
}
