-- V7__add_diet_log.sql
-- diet_logs: 식단 기록 (AI 파싱 / 수동 입력 / DB 조회 통합)
-- 호환 대상: MariaDB (운영/로컬), H2 in MySQL mode (테스트)

CREATE TABLE IF NOT EXISTS diet_logs (
    id               CHAR(36)      NOT NULL,
    user_id          CHAR(36)      NOT NULL,
    log_date         DATE          NOT NULL,
    meal_type        VARCHAR(20),
    logged_at        DATETIME,
    food_name        VARCHAR(255)  NOT NULL,
    amount_g         DECIMAL(8,1),
    amount_raw       VARCHAR(50),
    kcal             INT           NOT NULL,
    protein_g        DECIMAL(6,1),
    carbs_g          DECIMAL(6,1),
    fat_g            DECIMAL(6,1),
    source           VARCHAR(20)   NOT NULL,
    is_deleted       TINYINT(1)    NOT NULL DEFAULT 0,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip       VARCHAR(255),
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_ip       VARCHAR(255),
    deleted_at       DATETIME,
    deleted_ip       VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE INDEX idx_diet_logs_user_date ON diet_logs (user_id, log_date);
