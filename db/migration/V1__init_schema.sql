-- ============================================================
-- V1__init_schema.sql
-- DB: kookminfeed (PostgreSQL 15 + pgvector)
-- ============================================================

-- pgvector 확장 (keyword_embedding VECTOR(768) 사용)
CREATE EXTENSION IF NOT EXISTS vector;

-- ------------------------------------------------------------
-- 1. department (user_profile FK 선행 생성)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS department (
    code VARCHAR(20)  NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (code)
);

-- ------------------------------------------------------------
-- 2. user_profile
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_profile (
    user_id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    student_number            VARCHAR(20)   NOT NULL,
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    profile_completion_rate   SMALLINT      NULL,

    -- 학적 정보 (필수)
    grade                     SMALLINT      NOT NULL,
    department_code           VARCHAR(20)   NOT NULL,
    enrollment_status         VARCHAR(20)   NOT NULL DEFAULT 'enrolled',

    -- 관심·목표 (선택, 배열 상한은 애플리케이션에서 검증)
    interest_keywords         TEXT[]        NULL,  -- 최대 10개
    career_goals              TEXT[]        NULL,  -- 최대 5개
    course_interests          TEXT[]        NULL,  -- 최대 10개
    extracurricular_interests TEXT[]        NULL,  -- 최대 10개
    scholarship_interest      BOOLEAN       NULL   DEFAULT TRUE,

    -- 알림 선호
    notify_push               BOOLEAN       NOT NULL DEFAULT TRUE,
    notify_email              BOOLEAN       NOT NULL DEFAULT FALSE,
    notify_categories         TEXT[]        NULL,

    -- 운영/추천용 파생 컬럼 (시스템 자동 채움)
    keyword_embedding         VECTOR(768)   NULL,
    last_active_at            TIMESTAMPTZ   NULL,

    PRIMARY KEY (user_id),
    UNIQUE (student_number),
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

-- updated_at 자동 갱신 트리거
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_profile_updated_at
    BEFORE UPDATE ON user_profile
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- keyword_embedding 벡터 유사도 검색 인덱스 (IVFFlat)
CREATE INDEX IF NOT EXISTS idx_up_keyword_embedding
    ON user_profile USING ivfflat (keyword_embedding vector_cosine_ops);

-- ------------------------------------------------------------
-- 3. notice
-- ------------------------------------------------------------
CREATE TYPE notice_category AS ENUM ('학사','장학','비교과','취업','행사','기타');

CREATE TABLE IF NOT EXISTS notice (
    id           BIGSERIAL     NOT NULL,
    title        VARCHAR(500)  NOT NULL,
    link         VARCHAR(1000) NOT NULL,
    published    TIMESTAMPTZ   NOT NULL,
    source       VARCHAR(200)  NOT NULL,
    category     notice_category       DEFAULT '기타',
    importance   SMALLINT              DEFAULT 0,
    deadline     TIMESTAMPTZ,
    target_grade VARCHAR(20),
    collected_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id),
    CONSTRAINT uk_notice_link UNIQUE (link)
);

-- ------------------------------------------------------------
-- 4. notice_detail  (notice 와 1:1)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice_detail (
    id          BIGSERIAL NOT NULL,
    notice_id   BIGINT    NOT NULL,
    body        TEXT,
    attachments TEXT,
    summary     TEXT,

    PRIMARY KEY (id),
    UNIQUE (notice_id),
    CONSTRAINT fk_nd_notice
        FOREIGN KEY (notice_id) REFERENCES notice (id)
);
