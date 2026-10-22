package com.example.ddmsaa.gateway.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("GatewayErrorHandler: 例外から統一エラー応答への変換")
class GatewayErrorHandlerTest {

    private final GatewayErrorHandler handler = new GatewayErrorHandler(new ErrorResponseWriter());
    private final ObjectMapper mapper = new ObjectMapper();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    private MockServerWebExchange newExchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/unknown").build());
    }

    private JsonNode body(MockServerWebExchange exchange) throws Exception {
        return mapper.readTree(exchange.getResponse().getBodyAsString().block());
    }

    @ParameterizedTest(name = "ResponseStatusException({0}) は {1} になる")
    @CsvSource({"404,E0042", "403,E0002", "502,E0013", "504,E0043"})
    @DisplayName("ResponseStatusException の対応ステータスを messageId に変換する")
    void handle_responseStatusException_mapsMessageId(int code, String messageId)
            throws Exception {
        MockServerWebExchange exchange = newExchange();

        handler.handle(exchange, new ResponseStatusException(HttpStatus.valueOf(code))).block();

        assertThat(exchange.getResponse().getStatusCode().value()).isEqualTo(code);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo(messageId);
    }

    @Test
    @DisplayName("ConnectException は 502 / E0013 になる")
    void handle_connectException_returns502() throws Exception {
        MockServerWebExchange exchange = newExchange();

        handler.handle(exchange, new ConnectException("refused")).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo("E0013");
    }

    @Test
    @DisplayName("原因チェーン内の ConnectException も 502 / E0013 になる")
    void handle_wrappedConnectException_returns502() throws Exception {
        MockServerWebExchange exchange = newExchange();

        handler.handle(exchange, new RuntimeException("wrap", new ConnectException("refused")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo("E0013");
    }

    @Test
    @DisplayName("TimeoutException は 504 / E0043 になる")
    void handle_timeoutException_returns504() throws Exception {
        MockServerWebExchange exchange = newExchange();

        handler.handle(exchange, new RuntimeException("wrap", new TimeoutException("slow")))
                .block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo("E0043");
    }

    @Test
    @DisplayName("その他の例外は 500 / E0012 になる")
    void handle_otherException_returns500() throws Exception {
        MockServerWebExchange exchange = newExchange();

        handler.handle(exchange, new IllegalStateException("boom")).block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo("E0012");
    }

    @Test
    @DisplayName("対象外ステータスの ResponseStatusException は 500 / E0012 になる")
    void handle_unmappedResponseStatus_returns500() throws Exception {
        MockServerWebExchange exchange = newExchange();

        handler.handle(exchange, new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE))
                .block();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(body(exchange).get("messageId").asText()).isEqualTo("E0012");
    }

    @Test
    @DisplayName("traceId は Reactor Context を MDC より優先して取得する")
    void handle_traceId_prefersReactorContext() throws Exception {
        MockServerWebExchange exchange = newExchange();
        MDC.put("traceId", "from-mdc");

        handler.handle(exchange, new IllegalStateException("boom"))
                .contextWrite(ctx -> ctx.put("traceId", "from-context")).block();

        assertThat(body(exchange).get("traceId").asText()).isEqualTo("from-context");
    }

    @Test
    @DisplayName("Reactor Context に traceId が無い場合は MDC の値を使う")
    void handle_traceId_fallsBackToMdc() throws Exception {
        MockServerWebExchange exchange = newExchange();
        MDC.put("traceId", "from-mdc");

        handler.handle(exchange, new IllegalStateException("boom")).block();

        assertThat(body(exchange).get("traceId").asText()).isEqualTo("from-mdc");
    }

    @Test
    @DisplayName("Context / MDC に無い場合は exchange 属性の traceId を使う")
    void handle_traceId_fallsBackToExchangeAttribute() throws Exception {
        MockServerWebExchange exchange = newExchange();
        exchange.getAttributes().put("traceId", "from-attribute");

        handler.handle(exchange, new IllegalStateException("boom")).block();

        assertThat(body(exchange).get("traceId").asText()).isEqualTo("from-attribute");
    }

    @Test
    @DisplayName("どこにも無い場合は受信 X-Request-Id を traceId として使う")
    void handle_traceId_fallsBackToRequestIdHeader() throws Exception {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/x").header("X-Request-Id", "from-header").build());

        handler.handle(exchange, new IllegalStateException("boom")).block();

        assertThat(body(exchange).get("traceId").asText()).isEqualTo("from-header");
    }

    @Test
    @DisplayName("応答コミット済みの場合は例外をそのまま再送出する")
    void handle_committedResponse_rethrows() {
        MockServerWebExchange exchange = newExchange();
        exchange.getResponse().setComplete().block();
        IllegalStateException ex = new IllegalStateException("late");

        Mono<Void> result = handler.handle(exchange, ex);

        StepVerifier.create(result).expectErrorMatches(e -> e == ex).verify();
    }
}
