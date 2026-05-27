-- V1__init_schema.sql
-- Fit-Core 초기 전체 스키마
-- 호환 대상: MariaDB (운영/로컬), H2 in MySQL mode (테스트)
-- 주의: MariaDB 전용 구문(json_valid, ENGINE=InnoDB, CHARSET, COLLATE) 제거
--       ON UPDATE CURRENT_TIMESTAMP 제거 (Hibernate @UpdateTimestamp 처리)

-- =============================================================
-- 1. user_profiles  (부모 테이블)
-- =============================================================
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id                   CHAR(36)      NOT NULL,
    email                     VARCHAR(255)  NOT NULL,
    name                      VARCHAR(255),
    nickname                  VARCHAR(255),
    profile_image_url         VARCHAR(255),
    gender                    VARCHAR(10)   DEFAULT 'NONE',
    status                    VARCHAR(10)   DEFAULT 'ACTIVE',
    goal_type                 VARCHAR(20),
    split_type                VARCHAR(30),
    experience_level          VARCHAR(20),
    birth_date                DATE,
    training_days_per_week    INT,
    split_label               VARCHAR(50),
    body_weight_kg            DECIMAL(5,2),
    body_fat_pct              DECIMAL(5,2),
    notes                     TEXT,
    profile_version           INT           NOT NULL DEFAULT 1,
    time_available            INT,
    available_days            TEXT,
    equipment_access          TEXT,
    unpreferred_exercise_ids  TEXT,
    preferred_exercise_ids    TEXT,
    pain_areas                TEXT,
    doms                      TEXT,
    strength_baseline         TEXT,
    body_composition_snapshot TEXT,
    is_deleted                TINYINT(1)    NOT NULL DEFAULT 0,
    created_at                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip                VARCHAR(255),
    updated_at                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_ip                VARCHAR(255),
    deleted_at                DATETIME,
    deleted_ip                VARCHAR(255),
    PRIMARY KEY (user_id),
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE INDEX idx_user_status  ON user_profiles (status);
CREATE INDEX idx_user_deleted ON user_profiles (is_deleted, email);

-- =============================================================
-- 2. social_accounts  (FK → user_profiles)
-- =============================================================
CREATE TABLE IF NOT EXISTS social_accounts (
    id          CHAR(36)      NOT NULL,
    user_id     CHAR(36)      NOT NULL,
    provider    VARCHAR(255)  NOT NULL,
    provider_id VARCHAR(255)  NOT NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip  VARCHAR(255),
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_ip  VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT social_accounts_provider_id_IDX UNIQUE (provider, provider_id),
    CONSTRAINT fk_social_user
        FOREIGN KEY (user_id) REFERENCES user_profiles (user_id) ON DELETE CASCADE
);

CREATE INDEX idx_social_user_id ON social_accounts (user_id);

-- =============================================================
-- 3. exercise_tier  (마스터 데이터 — FK 없음)
-- =============================================================
CREATE TABLE IF NOT EXISTS exercise_tier (
    id               BIGINT  NOT NULL AUTO_INCREMENT,
    name_kr          TEXT,
    name_en          TEXT,
    primary_muscle   TEXT,
    secondary_muscle TEXT,
    equipment_req    TEXT,
    difficulty_tier  BIGINT,
    efficiency_tier  BIGINT,
    pain_triggers    TEXT,
    movement_type    TEXT,
    substitute_exercise_ids TEXT,
    PRIMARY KEY (id)
);

-- =============================================================
-- 4. routine_drafts  (AI 루틴 초안 — 독립 테이블)
-- =============================================================
CREATE TABLE IF NOT EXISTS routine_drafts (
    routine_draft_id           CHAR(36)     NOT NULL,
    user_id                    VARCHAR(36)  NOT NULL,
    source_profile_version     INT          NOT NULL,
    source_workout_session_ids TEXT,
    target_split_label         VARCHAR(64)  NOT NULL,
    generation_status          VARCHAR(16)  NOT NULL,
    status_reason_code         VARCHAR(32)  NOT NULL,
    is_fallback                TINYINT(1)   NOT NULL,
    request_payload_snapshot   TEXT         NOT NULL,
    response_payload_snapshot  TEXT         NOT NULL,
    adapter_request_snapshot   TEXT,
    adapter_response_snapshot  TEXT,
    rationale_summary          TEXT         NOT NULL,
    created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (routine_draft_id)
);

CREATE INDEX idx_routine_drafts_user_created ON routine_drafts (user_id, created_at);

-- =============================================================
-- 5. routine_finals  (저장된 루틴 최종본 — FK → routine_drafts)
-- =============================================================
CREATE TABLE IF NOT EXISTS routine_finals (
    routine_final_id        CHAR(36)     NOT NULL,
    routine_draft_id        CHAR(36)     NOT NULL,
    user_id                 VARCHAR(36)  NOT NULL,
    target_workout_date     DATE,
    target_split_label      VARCHAR(64)  NOT NULL,
    final_routine_payload   TEXT         NOT NULL,
    accepted_without_edits  TINYINT(1),
    user_edit_summary       TEXT,
    saved_at                DATETIME     NOT NULL,
    PRIMARY KEY (routine_final_id),
    CONSTRAINT uq_routine_finals_draft UNIQUE (routine_draft_id),
    CONSTRAINT fk_routine_finals_draft
        FOREIGN KEY (routine_draft_id) REFERENCES routine_drafts (routine_draft_id)
);

CREATE INDEX idx_routine_finals_user_saved ON routine_finals (user_id, saved_at);

-- =============================================================
-- 6. workout_sessions  (운동 세션 헤더)
-- =============================================================
CREATE TABLE IF NOT EXISTS workout_sessions (
    workout_session_id       CHAR(36)     NOT NULL,
    user_id                  VARCHAR(36)  NOT NULL,
    workout_date             DATE         NOT NULL,
    split_label              VARCHAR(64),
    source_routine_final_id  CHAR(36),
    time_available_min       SMALLINT,
    duration_min             SMALLINT,
    readiness_level          VARCHAR(16)  NOT NULL DEFAULT 'normal',
    current_pain_areas       TEXT,
    doms                     TEXT,
    unavailable_equipment    TEXT,
    session_note             TEXT,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (workout_session_id)
);

CREATE INDEX idx_workout_sessions_user_date    ON workout_sessions (user_id, workout_date);
CREATE INDEX idx_workout_sessions_source_final ON workout_sessions (source_routine_final_id);

-- =============================================================
-- 7. workout_sets  (세트 기록 — FK → workout_sessions)
-- =============================================================
CREATE TABLE IF NOT EXISTS workout_sets (
    workout_set_id         CHAR(36)      NOT NULL,
    workout_session_id     CHAR(36)      NOT NULL,
    exercise_order         INT,
    exercise_id            VARCHAR(128),
    exercise_name_snapshot VARCHAR(128)  NOT NULL,
    set_index              INT           NOT NULL,
    set_type               VARCHAR(16)   NOT NULL DEFAULT 'working',
    tracking_mode          VARCHAR(32)   NOT NULL DEFAULT 'weightReps',
    weight_kg              DECIMAL(6,2),
    reps                   INT           NOT NULL,
    rpe                    DECIMAL(3,1),
    rir                    DECIMAL(3,1),
    is_failure             TINYINT(1)    NOT NULL DEFAULT 0,
    rest_sec               INT,
    set_note               TEXT,
    created_at             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (workout_set_id),
    CONSTRAINT fk_workout_sets_session
        FOREIGN KEY (workout_session_id) REFERENCES workout_sessions (workout_session_id) ON DELETE CASCADE
);

CREATE INDEX idx_workout_sets_session_order ON workout_sets (workout_session_id, exercise_order, set_index);
