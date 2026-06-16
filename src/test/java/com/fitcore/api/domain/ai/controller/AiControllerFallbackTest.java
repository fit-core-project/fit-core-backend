package com.fitcore.api.domain.ai.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.ConnectException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitcore.api.infrastructure.ai.enums.StatusReasonCode;
import com.fitcore.api.infrastructure.ai.fallback.AiFailureClassifier;
import com.fitcore.api.infrastructure.ai.fallback.AiFallbackResponseFactory;

class AiControllerFallbackTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void forwardsSupplementChatJsonBodyToAi() throws Exception {
        ControllerFixture fixture = controllerWithMockServer();
        String requestBody = "{\"question\":\"와파린 먹는데 오메가3 먹어도 돼?\"}";
        String aiResponse = """
            {
              "answer": "mock answer",
              "sources": [
                {
                  "file": "supplement_interaction_rules.json",
                  "id": "INT_OMEGA3_ANTICOAGULANTS",
                  "type": "interaction_rule"
                }
              ],
              "mode": "full",
              "caution": "mock caution"
            }
            """;

        fixture.server.expect(once(), requestTo("http://ai.test/api/ai/supplement-chat"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(requestBody))
            .andRespond(withSuccess(aiResponse, MediaType.APPLICATION_JSON));

        var response = fixture.controller.supplementChat(requestBody);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("mode").asText()).isEqualTo("full");
        assertThat(body.path("fallback_reason").isMissingNode()).isTrue();
        assertThat(body.path("sources").get(0).path("id").asText()).isEqualTo("INT_OMEGA3_ANTICOAGULANTS");
        fixture.server.verify();
    }

    @Test
    void supplementEmptyBodyDoesNotCallAiUpstream() throws Exception {
        ControllerFixture fixture = controllerWithMockServer();

        var response = fixture.controller.supplementChat("");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("mode").asText()).isEqualTo("fallback");
        assertThat(body.path("fallback_reason").asText()).isEqualTo("ai_bad_response");
        fixture.server.verify();
    }

    @Test
    void supplementMalformedJsonKeepsRawForwardingPolicy() throws Exception {
        ControllerFixture fixture = controllerWithMockServer();
        String requestBody = "{\"question\":\"magnesium\"";
        String aiResponse = """
            {
              "answer": "mock answer",
              "sources": [],
              "mode": "full"
            }
            """;

        fixture.server.expect(once(), requestTo("http://ai.test/api/ai/supplement-chat"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string(requestBody))
            .andRespond(withSuccess(aiResponse, MediaType.APPLICATION_JSON));

        var response = fixture.controller.supplementChat(requestBody);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("mode").asText()).isEqualTo("full");
        fixture.server.verify();
    }

    @Test
    void quicklogAi500ReturnsFallback200() throws Exception {
        ControllerFixture fixture = controllerWithMockServer();
        fixture.server.expect(once(), anything()).andRespond(withServerError());

        var response = fixture.controller.parseLog("{\"text\":\"bench 3x10\"}");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("status").asText()).isEqualTo("fallback");
        assertThat(body.path("fallback_reason").asText()).isEqualTo("ai_remote_error");
    }

    @Test
    void supplementMalformedResponseReturnsFallback200() throws Exception {
        ControllerFixture fixture = controllerWithMockServer();
        fixture.server.expect(once(), anything()).andRespond(withSuccess("not-json", MediaType.TEXT_PLAIN));

        var response = fixture.controller.supplementChat("{\"question\":\"magnesium\"}");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("mode").asText()).isEqualTo("fallback");
        assertThat(body.path("fallback_reason").asText()).isEqualTo("ai_schema_mismatch");
    }

    @Test
    void sttAi500ReturnsUnavailable200() throws Exception {
        ControllerFixture fixture = controllerWithMockServer();
        fixture.server.expect(once(), anything()).andRespond(withServerError());
        MockMultipartFile audio = new MockMultipartFile("audio_file", "voice.webm", "audio/webm", new byte[] {1, 2, 3});

        var response = fixture.controller.speechToText(audio);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.path("status").asText()).isEqualTo("unavailable");
        assertThat(body.path("fallbackReason").asText()).isEqualTo("ai_remote_error");
    }

    @Test
    void connectionRefusedClassifiesAsConnectionRefused() {
        StatusReasonCode reason = AiFailureClassifier.classify(
            new ResourceAccessException("I/O error", new ConnectException("Connection refused"))
        );

        assertThat(reason).isEqualTo(StatusReasonCode.ai_connection_refused);
    }

    private ControllerFixture controllerWithMockServer() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ai.test/api/ai");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiController controller = new AiController(
            builder.build(),
            objectMapper,
            new AiFallbackResponseFactory(objectMapper)
        );
        return new ControllerFixture(controller, server);
    }

    private record ControllerFixture(AiController controller, MockRestServiceServer server) {
    }
}
