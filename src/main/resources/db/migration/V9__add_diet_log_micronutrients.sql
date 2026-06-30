-- V9__add_diet_log_micronutrients.sql
-- diet_logs: 당류(sugar_g), 식이섬유(fiber_g), 나트륨(sodium_mg) 추가
-- NULL 허용 — 미상(AI 파싱 출처 등) 항목은 null 저장
-- 호환 대상: MariaDB (운영/로컬), H2 in MySQL mode (테스트)
-- H2는 ADD COLUMN 단일 구문만 지원 — 별도 3줄로 분리

ALTER TABLE diet_logs ADD COLUMN sugar_g   DECIMAL(6,1) NULL;
ALTER TABLE diet_logs ADD COLUMN fiber_g   DECIMAL(6,1) NULL;
ALTER TABLE diet_logs ADD COLUMN sodium_mg INT          NULL;
