-- Enforce at most one ACTIVE routine program per user.
-- NULL values are not considered duplicates in unique indexes (MySQL/MariaDB/H2 behaviour).
ALTER TABLE routine_programs
    ADD COLUMN active_user_sentinel VARCHAR(36)
        GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN user_id ELSE NULL END);

CREATE UNIQUE INDEX uk_routine_programs_active_user
    ON routine_programs(active_user_sentinel);
