-- id 109 "이너 타이" (Inner Thigh, hip adduction machine) was seeded with primary_muscle
-- 'quadriceps'. It trains the adductors, like id 113 "이너 타이 (머신 힙 어덕션)".
-- Source of truth fixed in fit-core-ai/scripts/exercise_tier.xlsx (2026-11-11). The wrong
-- label made it the only "quad" exercise left for knee-pain users.
UPDATE exercise_tier
SET primary_muscle = 'adductor'
WHERE id = 109
  AND name_en = 'Inner Thigh'
  AND primary_muscle = 'quadriceps';
