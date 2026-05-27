-- [SUPERSEDED] 이 파일은 Flyway 도입으로 더 이상 사용되지 않습니다.
-- 현재 스키마 정의는 db/migration/V1__init_schema.sql 을 참조하세요.
-- spring.sql.init.mode=never 로 비활성화된 상태입니다.

-- 1. 부모 테이블: 유저 프로필
CREATE TABLE IF NOT EXISTS `user_profiles` (
     `user_id` char(36) NOT NULL COMMENT 'PK',
     `email` varchar(255) NOT NULL COMMENT '이메일(로그인 ID)',
     `name` varchar(255) DEFAULT NULL COMMENT '이름',
     `nickname` varchar(255) DEFAULT NULL COMMENT '닉네임',
     `profile_image_url` varchar(255) DEFAULT NULL COMMENT '프로필 사진 링크',
     `goal_type` enum('strength','hypertrophy','fatLoss','recomposition','generalFitness') DEFAULT NULL,
     `split_type` enum('fullBody','upperLower','pushPullLegs','bodyPartSplit','custom') DEFAULT NULL,
     `experience_level` enum('beginner','intermediate','advanced') DEFAULT NULL,
     `training_days_per_week` int(11) DEFAULT NULL,
     `split_label` varchar(50) DEFAULT NULL,
     `body_weight_kg` decimal(5,2) DEFAULT NULL,
     `body_fat_pct` decimal(5,2) DEFAULT NULL,
     `available_days` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`available_days`)),
     `equipment_access` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
     `unpreferred_exercise_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`unpreferred_exercise_ids`)),
     `preferred_exercise_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`preferred_exercise_ids`)),
     `pain_areas` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`pain_areas`)),
     `doms` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`doms`)),
     `strength_baseline` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
     `time_available` int(10) unsigned DEFAULT NULL COMMENT '운동 사용 가능한 시간',
     `body_composition_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
     `profile_version` int(11) NOT NULL DEFAULT 1,
     `gender` enum('MALE','FEMALE','NONE') DEFAULT 'NONE' COMMENT '성별',
     `birth_date` date DEFAULT NULL COMMENT '생년월일',
     `status` enum('ACTIVE','SLEEP','BANNED') DEFAULT 'ACTIVE' COMMENT '회원 상태',
     `notes` text DEFAULT NULL,
     `is_deleted` tinyint(1) DEFAULT 0 COMMENT '삭제 여부 (true: 삭제)',
     `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6) COMMENT '등록 일시',
     `created_ip` varchar(255) DEFAULT NULL,
     `updated_at` datetime(6) NOT NULL DEFAULT current_timestamp(6) ON UPDATE current_timestamp(6) COMMENT '마지막 수정 일시',
     `updated_ip` varchar(255) DEFAULT NULL,
     `deleted_at` datetime(6) DEFAULT NULL COMMENT '삭제 처리 일시',
     `deleted_ip` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`user_id`),
     UNIQUE KEY `email` (`email`),
     KEY `idx_user_email` (`email`),
     KEY `idx_user_status` (`status`),
     KEY `idx_user_deleted` (`is_deleted`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 자식 테이블: 소셜 계정
CREATE TABLE IF NOT EXISTS `social_accounts` (
    `id` char(36) NOT NULL,
    `user_id` char(36) NOT NULL,
    `provider` varchar(255) NOT NULL,
    `provider_id` varchar(255) NOT NULL,
    `created_at` datetime(6) NOT NULL DEFAULT current_timestamp(6),
    `created_ip` varchar(255) DEFAULT NULL,
    `updated_at` datetime(6) NOT NULL DEFAULT current_timestamp(6) ON UPDATE current_timestamp(6),
    `updated_ip` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `social_accounts_provider_id_IDX` (`provider_id`,`provider`) USING BTREE,
    KEY `fk_social_user` (`user_id`),
    CONSTRAINT `fk_social_user` FOREIGN KEY (`user_id`) REFERENCES `user_profiles` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 루틴 초안
CREATE TABLE IF NOT EXISTS `routine_drafts` (
    `routine_draft_id` char(36) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `source_profile_version` int(11) NOT NULL,
    `source_workout_session_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`source_workout_session_ids`)),
    `target_split_label` varchar(64) NOT NULL,
    `generation_status` varchar(16) NOT NULL,
    `status_reason_code` varchar(32) NOT NULL,
    `is_fallback` tinyint(1) NOT NULL,
    `request_payload_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`request_payload_snapshot`)),
    `response_payload_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`response_payload_snapshot`)),
    `adapter_request_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`adapter_request_snapshot`)),
    `adapter_response_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`adapter_response_snapshot`)),
    `rationale_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`rationale_summary`)),
    `created_at` datetime(3) NOT NULL,
    PRIMARY KEY (`routine_draft_id`),
    KEY `idx_routine_drafts_user_created` (`user_id`,`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 루틴 최종본
CREATE TABLE IF NOT EXISTS `routine_finals` (
    `routine_final_id` char(36) NOT NULL,
    `routine_draft_id` char(36) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `target_workout_date` date DEFAULT NULL,
    `target_split_label` varchar(64) NOT NULL,
    `final_routine_payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`final_routine_payload`)),
    `accepted_without_edits` tinyint(1) DEFAULT NULL,
    `user_edit_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`user_edit_summary`)),
    `saved_at` datetime(3) NOT NULL,
    PRIMARY KEY (`routine_final_id`),
    UNIQUE KEY `uq_routine_finals_draft` (`routine_draft_id`),
    KEY `idx_routine_finals_user_saved` (`user_id`,`saved_at` DESC),
    CONSTRAINT `fk_routine_finals_draft` FOREIGN KEY (`routine_draft_id`) REFERENCES `routine_drafts` (`routine_draft_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 운동 세션
CREATE TABLE IF NOT EXISTS `workout_sessions` (
    `workout_session_id` char(36) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `workout_date` date NOT NULL,
    `split_label` varchar(64) DEFAULT NULL,
    `source_routine_final_id` char(36) DEFAULT NULL,
    `time_available_min` smallint(6) DEFAULT NULL,
    `duration_min` smallint(6) DEFAULT NULL,
    `readiness_level` varchar(16) NOT NULL DEFAULT 'normal',
    `current_pain_areas` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`current_pain_areas`)),
    `doms` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`doms`)),
    `unavailable_equipment` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`unavailable_equipment`)),
    `session_note` text DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    `updated_at` datetime(3) NOT NULL,
    PRIMARY KEY (`workout_session_id`),
    KEY `idx_workout_sessions_user_date` (`user_id`,`workout_date` DESC),
    KEY `idx_workout_sessions_source_final` (`source_routine_final_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 운동 세트
CREATE TABLE IF NOT EXISTS `workout_sets` (
    `workout_set_id` char(36) NOT NULL,
    `workout_session_id` char(36) NOT NULL,
    `exercise_order` int(11) DEFAULT NULL,
    `exercise_id` varchar(128) DEFAULT NULL,
    `exercise_name_snapshot` varchar(128) NOT NULL,
    `set_index` int(11) NOT NULL,
    `set_type` varchar(16) NOT NULL DEFAULT 'working',
    `tracking_mode` varchar(32) NOT NULL DEFAULT 'weightReps',
    `weight_kg` decimal(6,2) DEFAULT NULL,
    `reps` int(11) NOT NULL,
    `rpe` decimal(3,1) DEFAULT NULL,
    `rir` decimal(3,1) DEFAULT NULL,
    `is_failure` tinyint(1) NOT NULL DEFAULT 0,
    `rest_sec` int(11) DEFAULT NULL,
    `set_note` text DEFAULT NULL,
    `created_at` datetime(3) NOT NULL,
    PRIMARY KEY (`workout_set_id`),
    KEY `idx_workout_sets_session_order` (`workout_session_id`,`exercise_order`,`set_index`),
    CONSTRAINT `fk_workout_sets_session` FOREIGN KEY (`workout_session_id`) REFERENCES `workout_sessions` (`workout_session_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;