package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V5__seed_exercise_substitute_mappings extends BaseJavaMigration {
    private static final int MAX_SUBSTITUTES = 6;

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<ExerciseRow> exercises = loadExercises(connection);

        try (PreparedStatement update = connection.prepareStatement(
            "UPDATE exercise_tier SET substitute_exercise_ids = ? WHERE id = ?"
        )) {
            for (ExerciseRow source : exercises) {
                if (!isBlank(source.substituteExerciseIds())) {
                    continue;
                }
                String substituteIds = buildSubstituteIds(source, exercises);
                update.setString(1, substituteIds);
                update.setLong(2, source.id());
                update.addBatch();
            }
            update.executeBatch();
        }
    }

    private List<ExerciseRow> loadExercises(Connection connection) throws Exception {
        List<ExerciseRow> exercises = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("""
                 SELECT id, primary_muscle, equipment_req, movement_type,
                        difficulty_tier, efficiency_tier, substitute_exercise_ids
                 FROM exercise_tier
                 ORDER BY id
                 """)) {
            while (rs.next()) {
                exercises.add(new ExerciseRow(
                    rs.getLong("id"),
                    rs.getString("primary_muscle"),
                    rs.getString("equipment_req"),
                    rs.getString("movement_type"),
                    rs.getLong("difficulty_tier"),
                    rs.getLong("efficiency_tier"),
                    rs.getString("substitute_exercise_ids")
                ));
            }
        }
        return exercises;
    }

    private String buildSubstituteIds(ExerciseRow source, List<ExerciseRow> exercises) {
        return exercises.stream()
            .filter(candidate -> candidate.id() != source.id())
            .filter(candidate -> !isBlank(source.primaryMuscle()))
            .filter(candidate -> source.primaryMuscle().equals(candidate.primaryMuscle()))
            .sorted(Comparator
                .comparingInt((ExerciseRow candidate) -> score(source, candidate)).reversed()
                .thenComparingLong(ExerciseRow::id))
            .limit(MAX_SUBSTITUTES)
            .map(candidate -> String.valueOf(candidate.id()))
            .collect(Collectors.joining(", "));
    }

    private int score(ExerciseRow source, ExerciseRow candidate) {
        Set<String> sourceEquipment = equipmentTokens(source.equipmentReq());
        Set<String> candidateEquipment = equipmentTokens(candidate.equipmentReq());
        Set<String> sourceLoadable = withoutBodyweight(sourceEquipment);
        Set<String> candidateLoadable = withoutBodyweight(candidateEquipment);

        int score = 0;
        if (!isBlank(source.movementType()) && source.movementType().equals(candidate.movementType())) {
            score += 40;
        }
        if (!sourceLoadable.isEmpty() && !candidateLoadable.isEmpty() && disjoint(sourceLoadable, candidateLoadable)) {
            score += 35;
        }
        if (!candidateEquipment.isEmpty() && candidateEquipment.contains("BODYWEIGHT")) {
            score += 12;
        }
        score += Math.max(0, 12 - Math.abs((int) (source.difficultyTier() - candidate.difficultyTier())) * 3);
        score += Math.max(0, 8 - (int) candidate.efficiencyTier()) * 4;
        return score;
    }

    private Set<String> equipmentTokens(String raw) {
        if (isBlank(raw)) {
            return Set.of();
        }
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : raw.split(",")) {
            String normalized = token.trim().toUpperCase();
            if (!normalized.isBlank()) {
                tokens.add(normalized);
            }
        }
        return tokens;
    }

    private Set<String> withoutBodyweight(Set<String> tokens) {
        return tokens.stream()
            .filter(token -> !"BODYWEIGHT".equals(token))
            .collect(Collectors.toSet());
    }

    private boolean disjoint(Set<String> left, Set<String> right) {
        return left.stream().noneMatch(right::contains);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record ExerciseRow(
        long id,
        String primaryMuscle,
        String equipmentReq,
        String movementType,
        long difficultyTier,
        long efficiencyTier,
        String substituteExerciseIds
    ) {
    }
}
