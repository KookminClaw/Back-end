-- ============================================================
-- V1__init_schema.sql
-- DB: kookminfeed (MariaDB 10.5)
-- ============================================================

-- ------------------------------------------------------------
-- 1. department (UserProfile FK 선행 생성)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS department (
    code VARCHAR(20)  NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (code)
);

-- ------------------------------------------------------------
-- 2. user_profile
--    - UUID → CHAR(36)
--    - TEXT[] → JSON  (MariaDB JSON = LONGTEXT + JSON 함수)
--    - TIMESTAMPTZ → DATETIME
--    - VECTOR(768) 제거 → 벡터 DB(별도) 사용
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_profile (
    user_id                   CHAR(36)    NOT NULL,
    student_number            VARCHAR(20) NOT NULL,
    created_at                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP,
    profile_completion_rate   SMALLINT    NULL,

    -- 학적 정보 (필수)
    grade                     SMALLINT    NOT NULL,
    department_code           VARCHAR(20) NOT NULL,
    enrollment_status         VARCHAR(20) NOT NULL DEFAULT 'enrolled',

    -- 관심·목표 (선택, JSON 배열)
    interest_keywords         JSON        NULL,  -- 최대 10개
    career_goals              JSON        NULL,  -- 최대 5개
    course_interests          JSON        NULL,  -- 최대 10개
    extracurricular_interests JSON        NULL,  -- 최대 10개
    scholarship_interest      BOOLEAN     NULL   DEFAULT TRUE,

    -- 알림 선호
    notify_push               BOOLEAN     NOT NULL DEFAULT TRUE,
    notify_email              BOOLEAN     NOT NULL DEFAULT FALSE,
    notify_categories         JSON        NULL,

    -- 운영/추천용 파생 컬럼
    last_active_at            DATETIME    NULL,

    PRIMARY KEY (user_id),
    UNIQUE  KEY uq_student_number (student_number),
    CONSTRAINT fk_up_department
        FOREIGN KEY (department_code) REFERENCES department (code),
    CONSTRAINT chk_grade
        CHECK (grade BETWEEN 1 AND 5),
    CONSTRAINT chk_profile_completion
        CHECK (profile_completion_rate IS NULL
            OR profile_completion_rate BETWEEN 0 AND 100),
    CONSTRAINT chk_enrollment_status
        CHECK (enrollment_status IN ('enrolled', 'leave', 'graduated'))
);

-- ------------------------------------------------------------
-- 3. notice
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice (
    id           BIGINT                                              NOT NULL AUTO_INCREMENT,
    title        VARCHAR(500)                                        NOT NULL,
    link         VARCHAR(1000)                                       NOT NULL,
    published    DATETIME                                            NOT NULL,
    source       VARCHAR(200)                                        NOT NULL,
    category     ENUM('학사','장학','비교과','취업','행사','기타')   DEFAULT '기타',
    importance   TINYINT                                             DEFAULT 0,
    deadline     DATETIME,
    target_grade VARCHAR(20),

    PRIMARY KEY (id),
    UNIQUE KEY uq_link (link(255))
);

-- ------------------------------------------------------------
-- 4. notice_detail  (notice 와 1:1)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice_detail (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    notice_id   BIGINT   NOT NULL,
    body        LONGTEXT,
    attachments TEXT,
    summary     TEXT,

    PRIMARY KEY (id),
    UNIQUE  KEY uq_notice (notice_id),
    CONSTRAINT fk_nd_notice
        FOREIGN KEY (notice_id) REFERENCES notice (id)
);
