package com.fitcore.api.domain.routine.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.domain.routine.dto.Doms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RoutineGenerateRequest — schema independence")
class RoutineSchemaTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("Jackson deserialization")
    class Deserialization {

        @Test
        void deserializesCurrentPainAreasAndCurrentDomsAsSeparateFields() throws Exception {
            String json = """
                    {
                      "targetMuscles": ["chest", "triceps"],
                      "readinessLevel": "normal",
                      "timeAvailableMin": 60,
                      "currentPainAreas": ["lower-back", "front-deltoids"],
                      "currentDoms": [
                        {"bodyPart": "chest", "level": "mild"},
                        {"bodyPart": "quadriceps", "level": "moderate"}
                      ],
                      "unavailableEquipment": [],
                      "goal": "hypertrophy",
                      "userNote": ""
                    }
                    """;

            RoutineGenerateRequest req = mapper.readValue(json, RoutineGenerateRequest.class);

            assertThat(req.getCurrentPainAreas()).containsExactly("lower-back", "front-deltoids");
            assertThat(req.getCurrentDoms()).hasSize(2);
            assertThat(req.getCurrentDoms().get(0).getBodyPart()).isEqualTo("chest");
            assertThat(req.getCurrentDoms().get(0).getLevel()).isEqualTo("mild");
        }

        @Test
        void emptyPainAreasDoesNotAffectDoms() throws Exception {
            String json = """
                    {
                      "currentPainAreas": [],
                      "currentDoms": [{"bodyPart": "gluteal", "level": "severe"}],
                      "targetMuscles": [],
                      "timeAvailableMin": 60,
                      "goal": "hypertrophy",
                      "readinessLevel": "normal",
                      "unavailableEquipment": [],
                      "userNote": ""
                    }
                    """;

            RoutineGenerateRequest req = mapper.readValue(json, RoutineGenerateRequest.class);

            assertThat(req.getCurrentPainAreas()).isEmpty();
            assertThat(req.getCurrentDoms()).hasSize(1);
            assertThat(req.getCurrentDoms().get(0).getBodyPart()).isEqualTo("gluteal");
        }

        @Test
        void emptyDomsDoesNotAffectPainAreas() throws Exception {
            String json = """
                    {
                      "currentPainAreas": ["lower-back", "knees"],
                      "currentDoms": [],
                      "targetMuscles": [],
                      "timeAvailableMin": 60,
                      "goal": "hypertrophy",
                      "readinessLevel": "normal",
                      "unavailableEquipment": [],
                      "userNote": ""
                    }
                    """;

            RoutineGenerateRequest req = mapper.readValue(json, RoutineGenerateRequest.class);

            assertThat(req.getCurrentPainAreas()).containsExactly("lower-back", "knees");
            assertThat(req.getCurrentDoms()).isEmpty();
        }
    }

    @Nested
    @DisplayName("cross-contamination absence")
    class CrossContamination {

        @Test
        void painAreaBodyPartsMustNotAppearInDoms() {
            RoutineGenerateRequest req = new RoutineGenerateRequest(
                null,
                List.of("chest", "triceps"),
                "normal",
                60,
                List.of("lower-back", "front-deltoids"),
                List.of(
                    Doms.builder().bodyPart("chest").level("mild").build(),
                    Doms.builder().bodyPart("quadriceps").level("moderate").build()
                ),
                List.of(),
                "hypertrophy",
                ""
            );

            Set<String> domsBodyParts = req.getCurrentDoms().stream()
                .map(Doms::getBodyPart)
                .collect(Collectors.toSet());

            for (String painArea : req.getCurrentPainAreas()) {
                assertThat(domsBodyParts).doesNotContain(painArea);
            }
        }

        @Test
        void domsBodyPartsMustNotAppearInPainAreas() {
            RoutineGenerateRequest req = new RoutineGenerateRequest(
                null,
                List.of("lats", "biceps"),
                "normal",
                75,
                List.of("lower-back"),
                List.of(
                    Doms.builder().bodyPart("chest").level("mild").build(),
                    Doms.builder().bodyPart("quadriceps").level("severe").build()
                ),
                List.of(),
                "hypertrophy",
                ""
            );

            Set<String> painAreaSet = Set.copyOf(req.getCurrentPainAreas());

            for (Doms dom : req.getCurrentDoms()) {
                assertThat(painAreaSet).doesNotContain(dom.getBodyPart());
            }
        }
    }
}
