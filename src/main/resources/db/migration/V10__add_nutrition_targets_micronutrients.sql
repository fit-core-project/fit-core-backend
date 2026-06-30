-- V10__add_nutrition_targets_micronutrients.sql
-- nutrition_targets: 당류상한(sugar_max), 식이섬유하한(fiber_min), 나트륨상한(sodium_max) 추가
-- NULL 허용 — 미설정 시 null
-- 호환 대상: MariaDB (운영/로컬), H2 in MySQL mode (테스트)

ALTER TABLE nutrition_targets ADD COLUMN sugar_max   DECIMAL(6,1) NULL;
ALTER TABLE nutrition_targets ADD COLUMN fiber_min   DECIMAL(6,1) NULL;
ALTER TABLE nutrition_targets ADD COLUMN sodium_max  INT          NULL;
