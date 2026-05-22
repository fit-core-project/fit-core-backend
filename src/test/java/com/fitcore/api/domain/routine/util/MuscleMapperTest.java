package com.fitcore.api.domain.routine.util;

import com.fitcore.api.domain.routine.dto.Doms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MuscleMapperTest {

    @Nested
    @DisplayName("mapLevelToInt")
    class MapLevelToInt {

        @Test
        void mapsKnownLevels() {
            assertThat(MuscleMapper.mapLevelToInt("mild")).isEqualTo(1);
            assertThat(MuscleMapper.mapLevelToInt("moderate")).isEqualTo(2);
            assertThat(MuscleMapper.mapLevelToInt("severe")).isEqualTo(3);
        }

        @Test
        void isCaseInsensitiveForLevels() {
            assertThat(MuscleMapper.mapLevelToInt("MILD")).isEqualTo(1);
            assertThat(MuscleMapper.mapLevelToInt("MODERATE")).isEqualTo(2);
            assertThat(MuscleMapper.mapLevelToInt("SEVERE")).isEqualTo(3);
        }

        @Test
        void unknownLevelDefaultsToOne() {
            assertThat(MuscleMapper.mapLevelToInt("extreme")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("convertDomsToMap")
    class ConvertDomsToMap {

        @Test
        void nullAndEmptyInputReturnEmptyMap() {
            assertThat(MuscleMapper.convertDomsToMap(null)).isEmpty();
            assertThat(MuscleMapper.convertDomsToMap(List.of())).isEmpty();
        }

        @Test
        void convertsLevelOnlyAndPreservesBodyPartValue() {
            var doms = List.of(Doms.builder().bodyPart("gluteal").level("mild").build());
            assertThat(MuscleMapper.convertDomsToMap(doms)).containsExactlyEntriesOf(Map.of("gluteal", 1));
        }

        @Test
        void doesNotExpandAliasLikeValues() {
            var doms = List.of(
                Doms.builder().bodyPart("deltoids").level("moderate").build(),
                Doms.builder().bodyPart("knees").level("severe").build(),
                Doms.builder().bodyPart("lats").level("mild").build()
            );

            assertThat(MuscleMapper.convertDomsToMap(doms))
                .containsEntry("deltoids", 2)
                .containsEntry("knees", 3)
                .containsEntry("lats", 1)
                .doesNotContainKeys("front-deltoids", "back-deltoids", "quadriceps", "hamstring", "upper-back");
        }

        @Test
        void duplicateBodyPartKeepsMaxLevel() {
            var doms = List.of(
                Doms.builder().bodyPart("chest").level("mild").build(),
                Doms.builder().bodyPart("chest").level("severe").build()
            );
            assertThat(MuscleMapper.convertDomsToMap(doms)).containsExactlyEntriesOf(Map.of("chest", 3));
        }

        @Test
        void skipsIncompleteEntries() {
            var doms = List.of(
                Doms.builder().bodyPart(null).level("mild").build(),
                Doms.builder().bodyPart("chest").level(null).build(),
                Doms.builder().bodyPart("biceps").level("mild").build()
            );
            assertThat(MuscleMapper.convertDomsToMap(doms)).containsExactlyEntriesOf(Map.of("biceps", 1));
        }

        @Test
        void preservesBodyPartCase() {
            var doms = List.of(Doms.builder().bodyPart("Chest").level("MILD").build());
            assertThat(MuscleMapper.convertDomsToMap(doms)).containsExactlyEntriesOf(Map.of("Chest", 1));
        }
    }

    @Nested
    @DisplayName("normalizeTargetMuscles")
    class NormalizeTargetMuscles {

        @Test
        void nullAndEmptyInputReturnEmptyList() {
            assertThat(MuscleMapper.normalizeTargetMuscles(null)).isEmpty();
            assertThat(MuscleMapper.normalizeTargetMuscles(List.of())).isEmpty();
        }

        @Test
        void trimsFiltersAndDeduplicatesWithoutAliasMapping() {
            assertThat(MuscleMapper.normalizeTargetMuscles(List.of("  ", "chest", "chest", " lats ")))
                .containsExactly("chest", "lats");
        }

        @Test
        void preservesAliasLikeValuesAsInputValues() {
            assertThat(MuscleMapper.normalizeTargetMuscles(List.of("deltoids", "knees", "glutes")))
                .containsExactly("deltoids", "knees", "glutes");
        }
    }

    @Nested
    @DisplayName("normalizeDbMuscleEnum")
    class NormalizeDbMuscleEnum {

        @Test
        void trimsOnly() {
            assertThat(MuscleMapper.normalizeDbMuscleEnum("  chest  ")).isEqualTo("chest");
            assertThat(MuscleMapper.normalizeDbMuscleEnum("rotator-cuff")).isEqualTo("rotator-cuff");
        }
    }
}
