package com.fitcore.api.domain.routine;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.infrastructure.ai.dto.AiRoutineResponse;
import com.fitcore.api.infrastructure.ai.enums.GenerationStatus;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Golden Fixture 기반 BE Data Contract Test
 *
 * 목적: AI 엔진이 내려주는 응답 JSON이 AiRoutineResponse DTO로
 *       정확히 역직렬화되는지 검증한다.
 *
 * 픽스처 파일: src/test/resources/fixtures/routine_*.json
 * 픽스처에 실제 데이터를 채운 후 모든 테스트가 통과해야 한다.
 *
 * 실패 케이스 예상 (픽스처 비어있는 동안):
 *   - 모든 필드가 null로 역직렬화되어 assertThat assertions 실패
 */
@Disabled("Awaiting golden fixture population — see docs/evidence/index.md")
@DisplayName("AiRoutineResponse — Golden Fixture Contract")
class RoutineContractTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
                // 픽스처에 알 수 없는 필드가 추가되어도 테스트 깨지지 않도록 허용
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private AiRoutineResponse loadFixture(String filename) throws Exception {
        String path = "/fixtures/" + filename;
        try (InputStream is = getClass().getResourceAsStream(path)) {
            assertThat(is)
                    .as("픽스처 파일을 찾을 수 없음: %s", path)
                    .isNotNull();
            return mapper.readValue(is, AiRoutineResponse.class);
        }
    }

    // -----------------------------------------------------------------------
    // Success Case — LLM 정상 생성
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Success Response")
    class SuccessContract {

        @Test
        @DisplayName("generationStatus가 success로 역직렬화된다")
        void generationStatusIsSuccess() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getGenerationStatus()).isEqualTo(GenerationStatus.success);
        }

        @Test
        @DisplayName("statusReasonCode가 none이다")
        void statusReasonCodeIsNone() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getStatusReasonCode()).isEqualTo(StatusReasonCode.none);
        }

        @Test
        @DisplayName("isFallback이 false이다")
        void isFallbackFalse() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getIsFallback()).isFalse();
        }

        @Test
        @DisplayName("routineDraftId가 null이 아닌 비어있지 않은 문자열이다")
        void routineDraftIdPresent() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getRoutineDraftId())
                    .isNotNull()
                    .isNotBlank();
        }

        @Test
        @DisplayName("routineBlocks가 1개 이상이다")
        void routineBlocksNotEmpty() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getRoutineBlocks())
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @DisplayName("각 routineBlock에 exerciseId, exerciseName, prescription이 있다")
        void eachBlockHasRequiredFields() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getRoutineBlocks()).allSatisfy(block -> {
                assertThat(block.getExerciseId()).isNotBlank();
                assertThat(block.getExerciseName()).isNotBlank();
                assertThat(block.getPrescription()).isNotEmpty();
            });
        }

        @Test
        @DisplayName("warnings 필드가 null이 아닌 리스트로 역직렬화된다")
        void warningsDeserializedAsList() throws Exception {
            AiRoutineResponse response = loadFixture("routine_success.json");
            assertThat(response.getWarnings()).isNotNull();
        }
    }

    // -----------------------------------------------------------------------
    // Fallback Case — 규칙 기반 보수적 대체
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Fallback Response")
    class FallbackContract {

        @Test
        @DisplayName("generationStatus가 fallback으로 역직렬화된다")
        void generationStatusIsFallback() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getGenerationStatus()).isEqualTo(GenerationStatus.fallback);
        }

        @Test
        @DisplayName("isFallback이 true이다")
        void isFallbackTrue() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getIsFallback()).isTrue();
        }

        @Test
        @DisplayName("statusReasonCode가 none이 아닌 구체적인 이유 코드다")
        void statusReasonCodeNotNone() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getStatusReasonCode())
                    .isNotNull()
                    .isNotEqualTo(StatusReasonCode.none);
        }

        @Test
        @DisplayName("warnings에 fallback 발동 이유가 1개 이상 포함된다")
        void warningsContainsFallbackReason() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getWarnings())
                    .isNotNull()
                    .isNotEmpty();
        }

        @Test
        @DisplayName("fallback에도 routineBlocks가 1개 이상 있다 (보수적 대체 운동 포함)")
        void fallbackStillHasBlocks() throws Exception {
            AiRoutineResponse response = loadFixture("routine_fallback.json");
            assertThat(response.getRoutineBlocks())
                    .isNotNull()
                    .isNotEmpty();
        }
    }

    // -----------------------------------------------------------------------
    // Failed Case — emptyCandidate: 안전 후보 없음
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("Failed Response (emptyCandidate)")
    class FailedContract {

        @Test
        @DisplayName("generationStatus가 failed로 역직렬화된다")
        void generationStatusIsFailed() throws Exception {
            AiRoutineResponse response = loadFixture("routine_failed.json");
            assertThat(response.getGenerationStatus()).isEqualTo(GenerationStatus.failed);
        }

        @Test
        @DisplayName("statusReasonCode가 emptyCandidate다")
        void statusReasonCodeIsEmptyCandidate() throws Exception {
            AiRoutineResponse response = loadFixture("routine_failed.json");
            assertThat(response.getStatusReasonCode()).isEqualTo(StatusReasonCode.emptyCandidate);
        }

        @Test
        @DisplayName("routineBlocks가 비어있다 (생성된 운동 없음)")
        void routineBlocksIsEmpty() throws Exception {
            AiRoutineResponse response = loadFixture("routine_failed.json");
            assertThat(response.getRoutineBlocks())
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("타입 불일치: 잘못된 generationStatus 값은 역직렬화 에러를 유발한다")
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
