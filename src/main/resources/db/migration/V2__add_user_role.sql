-- V2__add_user_role.sql
-- user_profiles 테이블에 role 컬럼 추가
ALTER TABLE user_profiles ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER';
