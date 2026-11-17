package com.fitcore.api.domain.user.purge;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
@ActiveProfiles("test")
class WithdrawnUserPurgeServiceTest {

    private static final List<String> USER_TABLES = List.of(
        "routine_programs", "workout_sessions", "routine_finals", "routine_drafts",
        "diet_logs", "nutrition_targets", "social_accounts", "user_profiles");

    @Autowired
    private WithdrawnUserPurgeService purgeService;

    @Autowired
    private WithdrawnUserDataPurger purger;

    @Autowired
    private JdbcTemplate jdbc;

    private final List<String> createdUsers = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (String userId : createdUsers) {
            jdbc.update("UPDATE user_profiles SET is_deleted = 1, deleted_at = ? WHERE user_id = ?",
                LocalDateTime.of(2000, 1, 1, 0, 0), userId);
            purger.purgeUser(userId, LocalDateTime.of(2100, 1, 1, 0, 0));
        }
    }

    @Test
    void purgeExpired_removesUserWithdrawn31DaysAgo_keeps29Days_andActiveUser() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 17, 4, 0);
        Seed expired = seedUser(now.minusDays(31));
        Seed recent = seedUser(now.minusDays(29));
        Seed active = seedUser(null);

        int purged = purgeService.purgeExpired(now);

        assertThat(purged).isGreaterThanOrEqualTo(1);
        assertAllGone(expired);
        assertAllPresent(recent);
        assertAllPresent(active);
        assertThat(jdbc.queryForObject(
            "SELECT is_deleted FROM user_profiles WHERE user_id = ?", Integer.class, active.userId))
            .isZero();
    }

    @Test
    void purgeExpired_alsoRemovesAiRoutineFeedbackWhenTheTableExists() {
        // the AI service creates routine_feedback in the same DB; the H2 test schema does not have it
        jdbc.execute("CREATE TABLE IF NOT EXISTS routine_feedback (id VARCHAR(36) PRIMARY KEY,"
            + " user_id VARCHAR(128), routine_draft_id VARCHAR(128) NOT NULL)");
        try {
            LocalDateTime now = LocalDateTime.of(2026, 11, 17, 4, 0);
            Seed expired = seedUser(now.minusDays(31));
            Seed active = seedUser(null);
            jdbc.update("INSERT INTO routine_feedback (id, user_id, routine_draft_id) VALUES (?, ?, 'd')",
                UUID.randomUUID().toString(), expired.userId);
            jdbc.update("INSERT INTO routine_feedback (id, user_id, routine_draft_id) VALUES (?, ?, 'd')",
                UUID.randomUUID().toString(), active.userId);
            // saved without user_id, tied only to the withdrawn user's draft
            String expiredDraft = jdbc.queryForObject(
                "SELECT routine_draft_id FROM routine_drafts WHERE user_id = ? LIMIT 1", String.class, expired.userId);
            jdbc.update("INSERT INTO routine_feedback (id, user_id, routine_draft_id) VALUES (?, NULL, ?)",
                UUID.randomUUID().toString(), expiredDraft);

            purgeService.purgeExpired(now);

            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM routine_feedback WHERE user_id = ?",
                Integer.class, expired.userId)).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM routine_feedback WHERE user_id IS NULL",
                Integer.class)).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM routine_feedback WHERE user_id = ?",
                Integer.class, active.userId)).isEqualTo(1);
        } finally {
            jdbc.execute("DROP TABLE IF EXISTS routine_feedback");
        }
    }

    @Test
    void purgeExpired_isIdempotent() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 17, 4, 0);
        Seed expired = seedUser(now.minusDays(40));

        purgeService.purgeExpired(now);
        int secondRun = purgeService.purgeExpired(now);

        assertThat(secondRun).isZero();
        assertAllGone(expired);
        assertThat(purger.purgeUser(expired.userId, now)).isEqualTo(Map.of());
    }

    @Test
    void purgeExpired_processesMoreUsersThanOneBatch() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 17, 4, 0);
        List<Seed> seeds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            seeds.add(seedUser(now.minusDays(60 + i)));
        }
        ReflectionTestUtils.setField(purgeService, "batchSize", 1);
        try {
            purgeService.purgeExpired(now);
        } finally {
            ReflectionTestUtils.setField(purgeService, "batchSize", 100);
        }

        seeds.forEach(this::assertAllGone);
    }

    // ---------------------------------------------------------------- helpers

    private record Seed(String userId, String sessionId, String programId) {
    }

    /** withdrawnAt 이 null 이면 활성 사용자. */
    private Seed seedUser(LocalDateTime withdrawnAt) {
        String userId = UUID.randomUUID().toString();
        createdUsers.add(userId);
        boolean withdrawn = withdrawnAt != null;

        jdbc.update("INSERT INTO user_profiles (user_id, email, nickname, status, is_deleted, deleted_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)",
            userId,
            withdrawn ? "withdrawn+" + userId + "@withdrawn.fitcore.invalid" : userId + "@fitcore.test",
            withdrawn ? null : "nick-" + userId.substring(0, 8),
            "ACTIVE",
            withdrawn ? 1 : 0,
            withdrawnAt);
        if (!withdrawn) {
            jdbc.update("INSERT INTO social_accounts (id, user_id, provider, provider_id) VALUES (?, ?, ?, ?)",
                UUID.randomUUID().toString(), userId, "kakao", "k-" + userId);
        }

        String draftId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO routine_drafts (routine_draft_id, user_id, source_profile_version,"
                + " target_split_label, generation_status, status_reason_code, is_fallback,"
                + " request_payload_snapshot, response_payload_snapshot, rationale_summary)"
                + " VALUES (?, ?, 1, 'push', 'success', 'none', 0, '{}', '{}', 'r')",
            draftId, userId);

        String finalId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO routine_finals (routine_final_id, routine_draft_id, user_id,"
                + " target_split_label, final_routine_payload, saved_at) VALUES (?, ?, ?, 'push', '{}', ?)",
            finalId, draftId, userId, LocalDateTime.now());

        String sessionId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO workout_sessions (workout_session_id, user_id, workout_date,"
                + " source_routine_final_id) VALUES (?, ?, ?, ?)",
            sessionId, userId, LocalDate.of(2026, 10, 1), finalId);
        jdbc.update("INSERT INTO workout_sets (workout_set_id, workout_session_id,"
                + " exercise_name_snapshot, set_index, reps) VALUES (?, ?, 'Bench Press', 1, 10)",
            UUID.randomUUID().toString(), sessionId);

        String programId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO routine_programs (program_id, user_id, name, status, current_position)"
                + " VALUES (?, ?, 'P', 'ACTIVE', 1)",
            programId, userId);
        String itemId = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO routine_program_items (program_item_id, program_id, position,"
                + " routine_final_id) VALUES (?, ?, 1, ?)",
            itemId, programId, finalId);
        jdbc.update("INSERT INTO routine_program_completion_events (event_id, program_id,"
                + " program_item_id, workout_session_id, completed_position) VALUES (?, ?, ?, ?, 1)",
            UUID.randomUUID().toString(), programId, itemId, sessionId);

        jdbc.update("INSERT INTO diet_logs (id, user_id, log_date, food_name, kcal, source)"
                + " VALUES (?, ?, ?, 'rice', 300, 'manual')",
            UUID.randomUUID().toString(), userId, LocalDate.of(2026, 10, 1));
        jdbc.update("INSERT INTO nutrition_targets (id, user_id, kcal_goal) VALUES (?, ?, 2000)",
            UUID.randomUUID().toString(), userId);

        return new Seed(userId, sessionId, programId);
    }

    private void assertAllGone(Seed seed) {
        for (String table : USER_TABLES) {
            assertThat(countByUser(table, seed.userId)).as(table).isZero();
        }
        assertThat(countWhere("workout_sets", "workout_session_id", seed.sessionId)).isZero();
        assertThat(countWhere("routine_program_items", "program_id", seed.programId)).isZero();
        assertThat(countWhere("routine_program_completion_events", "program_id", seed.programId)).isZero();
    }

    private void assertAllPresent(Seed seed) {
        for (String table : USER_TABLES) {
            if (table.equals("social_accounts")) {
                continue; // 탈퇴 시점에 이미 끊긴다.
            }
            assertThat(countByUser(table, seed.userId)).as(table).isEqualTo(1);
        }
        assertThat(countWhere("workout_sets", "workout_session_id", seed.sessionId)).isEqualTo(1);
        assertThat(countWhere("routine_program_items", "program_id", seed.programId)).isEqualTo(1);
        assertThat(countWhere("routine_program_completion_events", "program_id", seed.programId))
            .isEqualTo(1);
    }

    private int countByUser(String table, String userId) {
        return countWhere(table, "user_id", userId);
    }

    private int countWhere(String table, String column, String value) {
        Integer n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, value);
        return n == null ? 0 : n;
    }
}
