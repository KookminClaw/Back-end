package com.kookminclaw.backend.notice.repository;

import com.kookminclaw.backend.notice.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("select count(n) > 0 from Notice n where n.link = :sourceUrl")
    boolean existsBySourceUrl(@Param("sourceUrl") String sourceUrl);

    @Query("select n from Notice n where n.link = :sourceUrl")
    Optional<Notice> findBySourceUrl(@Param("sourceUrl") String sourceUrl);
}
