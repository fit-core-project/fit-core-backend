package com.fitcore.api.domain.routine;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AiRoutineResponse Golden Fixture Contract")
class RoutineContractTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private AiRoutineResponse loadFixture(String filename) throws Exception {
        String path = "/fixtures/" + filename;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            assertThat(is)
                .as("Fixture file must exist: %s", path)
                .isNotNull();
            return mapper.readValue(is, AiRoutineResponse.class);
        }
    }

    @Nested
    @DisplayName("Success Response")
    class SuccessContract {

        @Test
        @DisplayName("generationStatus is success")
        void generationStatusIsSuccess() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getGenerationStatus()).isEqualTo(GenerationStatus.success);
        }

        @Test
        @DisplayName("statusReasonCode is none")
        void statusReasonCodeIsNone() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getStatusReasonCode()).isEqualTo(StatusReasonCode.none);
        }

        @Test
        @DisplayName("isFallback is false")
        void isFallbackFalse() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getIsFallback()).isFalse();
        }

        @Test
        @DisplayName("routineDraftId is present")
        void routineDraftIdPresent() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getRoutineDraftId())
                .isNotNull()
                .isNotBlank();
        }

        @Test
        @DisplayName("routineBlocks is not empty")
        void routineBlocksNotEmpty() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getRoutineBlocks())
                .isNotNull()
                .isNotEmpty();
        }

        @Test
        @DisplayName("each routineBlock has required fields")
        void eachBlockHasRequiredFields() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getRoutineBlocks()).allSatisfy(block -> {
                assertThat(block.getExerciseId()).isNotBlank();
                assertThat(block.getExerciseName()).isNotBlank();
                assertThat(block.getPrescription()).isNotEmpty();
            });
        }

        @Test
        @DisplayName("warnings is deserialized as a non-null list")
        void warningsDeserializedAsList() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getWarnings()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Fallback Response")
    class FallbackContract {

        @Test
        @DisplayName("generationStatus is fallback")
        void generationStatusIsFallback() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getGenerationStatus()).isEqualTo(GenerationStatus.fallback);
        }

        @Test
        @DisplayName("isFallback is true")
        void isFallbackTrue() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getIsFallback()).isTrue();
        }

        @Test
        @DisplayName("statusReasonCode is a concrete non-none reason")
        void statusReasonCodeNotNone() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getStatusReasonCode())
                .isNotNull()
                .isNotEqualTo(StatusReasonCode.none);
        }

        @Test
        @DisplayName("warnings contains at least one fallback reason")
        void warningsContainsFallbackReason() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getWarnings())
                .isNotNull()
                .isNotEmpty();
        }

        @Test
        @DisplayName("fallback still has routineBlocks")
        void fallbackStillHasBlocks() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getRoutineBlocks())
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Failed Response")
    class FailedContract {

        @Test
        @DisplayName("generationStatus is failed")
        void generationStatusIsFailed() throws Exception {
            AiRoutineResponse response = loadFixture("routine_failed.json");
            assertThat(response.getGenerationStatus()).isEqualTo(GenerationStatus.failed);
        }

        @Test
        @DisplayName("statusReasonCode is emptyCandidate")
        void statusReasonCodeIsEmptyCandidate() throws Exception {
            AiRoutineResponse response = loadFixture("routine_failed.json");
            assertThat(response.getStatusReasonCode()).isEqualTo(StatusReasonCode.emptyCandidate);
        }

        @Test
        @DisplayName("routineBlocks is empty")
        void routineBlocksIsEmpty() throws Exception {
            AiRoutineResponse response = loadFixture("routine_failed.json");
            assertThat(response.getRoutineBlocks())
                .isNotNull()
                .isEmpty();
        }

        @Test
        @DisplayName("invalid generationStatus fails deserialization")
        void invalidGenerationStatusCausesDeserializationError() {
            String invalidJson = """
                {
                  "generationStatus": "INVALID_STATUS",
                  "statusReasonCode": "emptyCandidate",
                  "routineBlocks": []
                }
                """;
            org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> mapper.readValue(invalidJson, AiRoutineResponse.class)
            ).isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidFormatException.class);
        }
    }
}
