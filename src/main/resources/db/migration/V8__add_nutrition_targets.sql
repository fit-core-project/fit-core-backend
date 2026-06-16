-- V8__add_nutrition_targets.sql
-- nutrition_targets: 유저별 영양 목표 (1인 1행, 전 컬럼 NULL = 미설정)
-- 호환 대상: MariaDB (운영/로컬), H2 in MySQL mode (테스트)

CREATE TABLE IF NOT EXISTS nutrition_targets (
    id               CHAR(36)      NOT NULL,
    user_id          CHAR(36)      NOT NULL,
    kcal_goal        INT,
    protein_g_min    DECIMAL(6,1),
    protein_g_max    DECIMAL(6,1),
    carbs_g_min      DECIMAL(6,1),
    carbs_g_max      DECIMAL(6,1),
    fat_g_min        DECIMAL(6,1),
    fat_g_max        DECIMAL(6,1),
    is_deleted       TINYINT(1)    NOT NULL DEFAULT 0,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip       VARCHAR(255),
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_ip       VARCHAR(255),
    deleted_at       DATETIME,
    deleted_ip       VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_nutrition_targets_user UNIQUE (user_id)
);
