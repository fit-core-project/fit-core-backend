package com.fitcore.api.domain.user.purge;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 탈퇴 후 보관 기간이 지난 사용자 한 명의 데이터를 하드 삭제한다.
 *
 * <p>user_profiles 는 SoftDelete 엔티티라 JPA 로는 탈퇴 행이 보이지 않으므로 네이티브 SQL 을 쓴다.
 * 삭제 순서는 FK 방향(자식에서 부모)을 따른다. 사용자 단위로 트랜잭션을 묶어 일부만 지워진 상태가 남지 않게 하고,
 * 이미 지워졌거나 조건에 맞지 않는 사용자는 아무것도 하지 않으므로 재실행해도 안전하다.
 *
 * <p>사용자 소유 테이블(user_id 컬럼 보유 또는 그 자식)이 새로 생기면 DELETE_STEPS 에 추가해야 한다.
 */
@Component
@RequiredArgsConstructor
public class WithdrawnUserDataPurger {

    private static final String PROGRAMS_OF_USER =
        "SELECT program_id FROM routine_programs WHERE user_id = :userId";
    private static final String SESSIONS_OF_USER =
        "SELECT workout_session_id FROM workout_sessions WHERE user_id = :userId";

    /** FK 순서대로 정렬한 (집계 키, DELETE 문) 목록. user_profiles 는 마지막. */
    private static final List<Map.Entry<String, String>> DELETE_STEPS = List.of(
        Map.entry("routine_program_completion_events",
            "DELETE FROM routine_program_completion_events"
                + " WHERE program_id IN (" + PROGRAMS_OF_USER + ")"
                + " OR workout_session_id IN (" + SESSIONS_OF_USER + ")"),
        Map.entry("routine_program_items",
            "DELETE FROM routine_program_items WHERE program_id IN (" + PROGRAMS_OF_USER + ")"),
        Map.entry("routine_programs",
            "DELETE FROM routine_programs WHERE user_id = :userId"),
        Map.entry("workout_sets",
            "DELETE FROM workout_sets WHERE workout_session_id IN (" + SESSIONS_OF_USER + ")"),
        Map.entry("workout_sessions",
            "DELETE FROM workout_sessions WHERE user_id = :userId"),
        Map.entry("routine_finals",
            "DELETE FROM routine_finals WHERE user_id = :userId"),
        Map.entry("routine_drafts",
            "DELETE FROM routine_drafts WHERE user_id = :userId"),
        Map.entry("diet_logs",
            "DELETE FROM diet_logs WHERE user_id = :userId"),
        Map.entry("nutrition_targets",
            "DELETE FROM nutrition_targets WHERE user_id = :userId"),
        Map.entry("social_accounts",
            "DELETE FROM social_accounts WHERE user_id = :userId"),
        Map.entry("user_profiles",
            "DELETE FROM user_profiles WHERE user_id = :userId AND is_deleted = 1")
    );

    static final String AI_FEEDBACK_TABLE = "routine_feedback";

    private final NamedParameterJdbcTemplate jdbc;

    private boolean tableExists(String table) {
        Boolean exists = jdbc.getJdbcTemplate().execute((ConnectionCallback<Boolean>) connection -> {
            DatabaseMetaData meta = connection.getMetaData();
            for (String name : new String[] {table, table.toUpperCase(Locale.ROOT)}) {
                try (ResultSet rs = meta.getTables(connection.getCatalog(), null, name, new String[] {"TABLE"})) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            return false;
        });
        return Boolean.TRUE.equals(exists);
    }

    /** 보관 기간이 지난 탈퇴 사용자 id 를 오래된 순으로 최대 limit 건 조회한다. */
    @Transactional(readOnly = true)
    public List<String> findExpiredUserIds(LocalDateTime cutoff, int limit) {
        return jdbc.queryForList(
            "SELECT user_id FROM user_profiles"
                + " WHERE is_deleted = 1 AND deleted_at IS NOT NULL AND deleted_at <= :cutoff"
                + " ORDER BY deleted_at, user_id LIMIT :limit",
            new MapSqlParameterSource()
                .addValue("cutoff", cutoff)
                .addValue("limit", limit),
            String.class);
    }

    /**
     * 사용자 한 명을 하드 삭제한다. 트랜잭션 안에서 조건을 다시 확인하므로 그 사이 상태가 바뀐 사용자는 건드리지 않는다.
     *
     * @return 테이블별 삭제 건수. 조건에 맞지 않으면 빈 맵.
     */
    @Transactional
    public Map<String, Integer> purgeUser(String userId, LocalDateTime cutoff) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("cutoff", cutoff);

        Integer eligible = jdbc.queryForObject(
            "SELECT COUNT(*) FROM user_profiles"
                + " WHERE user_id = :userId AND is_deleted = 1"
                + " AND deleted_at IS NOT NULL AND deleted_at <= :cutoff",
            params, Integer.class);
        if (eligible == null || eligible == 0) {
            return Map.of();
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        // The AI server's own table in the same DB (fit-core-ai models/routine_feedback.py). It is created
        // by the AI service, so it may not exist (e.g. the H2 test schema); no FK to user_profiles.
        if (tableExists(AI_FEEDBACK_TABLE)) {
            // rows carry user_id, or only the routine_draft_id of one of the user's drafts
            counts.put(AI_FEEDBACK_TABLE, jdbc.update("DELETE FROM " + AI_FEEDBACK_TABLE
                + " WHERE user_id = :userId"
                + " OR routine_draft_id IN (SELECT routine_draft_id FROM routine_drafts WHERE user_id = :userId)", params));
        }
        for (Map.Entry<String, String> step : DELETE_STEPS) {
            counts.put(step.getKey(), jdbc.update(step.getValue(), params));
        }
        return counts;
    }
}
