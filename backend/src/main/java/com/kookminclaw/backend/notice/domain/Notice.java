package com.kookminclaw.backend.notice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, unique = true, length = 1000)
    private String link;

    @Column(nullable = false)
    private OffsetDateTime published;

    @Column(nullable = false, length = 200)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private NoticeCategory category = NoticeCategory.기타;

    @Builder.Default
    private Short importance = 0;

    private OffsetDateTime deadline;

    @Column(name = "target_grade", length = 20)
    private String targetGrade;

    @OneToOne(mappedBy = "notice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NoticeDetail detail;
}