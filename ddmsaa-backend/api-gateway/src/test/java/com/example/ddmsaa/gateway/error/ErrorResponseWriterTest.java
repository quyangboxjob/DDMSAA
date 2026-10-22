package com.example.ddmsaa.gateway.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

@DisplayName("ErrorResponseWriter: Gateway エラー応答の JSON 書き出し")
class ErrorResponseWriterTest {

    private final ErrorResponseWriter writer = new ErrorResponseWriter();
    private final ObjectMapper mapper = new ObjectMapper();

    private MockServerWebExchange newExchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/users").build());
    }

    @Test
    @DisplayName("指定したステータスと JSON Content-Type で応答する")
    void write_setsStatusAndContentType() {
        MockServerWebExchange exchange = newExchange();

        writer.write(exchange, HttpStatus.UNAUTHORIZED, "E0040", "trace123").block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType())
                .isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("ボディに messageId / timestamp / path / traceId の 4 項目のみを含む")
    void write_bodyContainsFourFields() throws Exception {
        MockServerWebExchange exchange = newExchange();

        writer.write(exchange, HttpStatus.UNAUTHORIZED, "E0040", "trace123").block();

        JsonNode json = mapper.readTree(exchange.getResponse().getBodyAsString().block());
        assertThat(json.fieldNames()).toIterable()
                .containsExactly("messageId", "timestamp", "path", "traceId");
        assertThat(json.get("messageId").asText()).isEqualTo("E0040");
        assertThat(json.get("path").asText()).isEqualTo("/api/v1/users");
        assertThat(json.get("traceId").asText()).isEqualTo("trace123");
    }

    @Test
    @DisplayName("timestamp は +09:00 オフセット付きの ISO 8601 形式である")
    void write_timestampHasJstOffset() throws Exception {
        MockServerWebExchange exchange = newExchange();

        writer.write(exchange, HttpStatus.NOT_FOUND, "E0042", "t").block();

        JsonNode json = mapper.readTree(exchange.getResponse().getBodyAsString().block());
        String timestamp = json.get("timestamp").asText();
        assertThat(timestamp).endsWith("+09:00");
        assertThat(OffsetDateTime.parse(timestamp).getOffset().getTotalSeconds())
                .isEqualTo(9 * 3600);
    }

    @Test
    @DisplayName("traceId が null の場合は JSON null として出力する")
    void write_nullTraceId_writesJsonNull() throws Exception {
        MockServerWebExchange exchange = newExchange();

        writer.write(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "E0012", null).block();

        JsonNode json = mapper.readTree(exchange.getResponse().getBodyAsString().block());
        assertThat(json.get("traceId").isNull()).isTrue();
    }

    @Test
    @DisplayName("応答コミット済みの場合は何も書き込まずに完了する")
    void write_committedResponse_doesNothing() {
        MockServerWebExchange exchange = newExchange();
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().setComplete().block();

        writer.write(exchange, HttpStatus.UNAUTHORIZED, "E0040", "t").block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getResponse().getBodyAsString().block()).isEmpty();
    }
}
