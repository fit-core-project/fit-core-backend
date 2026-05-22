package com.fitcore.api.domain.routine.util;

import com.fitcore.api.domain.routine.dto.Doms;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MuscleMapper {

    private MuscleMapper() {}

    public static int mapLevelToInt(String level) {
        return switch (level.toLowerCase()) {
            case "mild"     -> 1;
            case "moderate" -> 2;
            case "severe"   -> 3;
            default         -> 1;
        };
    }

    public static Map<String, Integer> convertDomsToMap(List<Doms> doms) {
        if (doms == null || doms.isEmpty()) return Collections.emptyMap();

        Map<String, Integer> result = new HashMap<>();
        for (Doms d : doms) {
            if (d.getBodyPart() == null || d.getLevel() == null) continue;
            int levelInt = mapLevelToInt(d.getLevel());
            String bodyPart = normalizeDbMuscleEnum(d.getBodyPart());
            if (!bodyPart.isBlank()) result.merge(bodyPart, levelInt, Math::max);
        }
        return result;
    }

    public static List<String> normalizeTargetMuscles(List<String> targetMuscles) {
        if (targetMuscles == null || targetMuscles.isEmpty()) return Collections.emptyList();
        return targetMuscles.stream()
            .filter(m -> m != null && !m.isBlank())
            .map(MuscleMapper::normalizeDbMuscleEnum)
            .distinct()
            .toList();
    }

    public static String normalizeDbMuscleEnum(String value) {
        return value.trim();
    }
}
