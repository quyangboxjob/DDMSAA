package com.example.ddmsaa.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@DisplayName("TraceIdGlobalFilter: traceId の確定と貫通")
class TraceIdGlobalFilterTest {

    private final TraceIdGlobalFilter filter = new TraceIdGlobalFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    private MockServerWebExchange newExchange(String requestId) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get("/api/v1/users");
        if (requestId != null) {
            builder.header("X-Request-Id", requestId);
        }
        return MockServerWebExchange.from(builder.build());
    }

    @Test
    @DisplayName("受信した X-Request-Id を traceId として採用し下流へ透過する")
    void filter_adoptsRequestId() {
        MockServerWebExchange exchange = newExchange("req-abc");
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        AtomicReference<String> mdcInChain = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstream.set(ex);
            mdcInChain.set(MDC.get("traceId"));
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(downstream.get().getRequest().getHeaders().getFirst("X-Request-Id"))
                .isEqualTo("req-abc");
        assertThat(mdcInChain.get()).isEqualTo("req-abc");
    }

    @Test
    @DisplayName("X-Request-Id が無い場合は 32 文字のハイフン無し UUID を生成する")
    void filter_generatesUuidWithoutHyphen() {
        MockServerWebExchange exchange = newExchange(null);
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstream.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        String generated = downstream.get().getRequest().getHeaders().getFirst("X-Request-Id");
        assertThat(generated).matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("X-Request-Id が空白の場合も UUID を生成する")
    void filter_blankRequestId_generatesUuid() {
        MockServerWebExchange exchange = newExchange("   ");
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        GatewayFilterChain chain = ex -> {
            downstream.set(ex);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(downstream.get().getRequest().getHeaders().getFirst("X-Request-Id"))
                .matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("応答コミット時に X-Trace-Id 応答ヘッダが付与される")
    void filter_setsTraceIdResponseHeaderBeforeCommit() {
        MockServerWebExchange exchange = newExchange("req-abc");
        GatewayFilterChain chain = ex -> ex.getResponse().setComplete();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders().getFirst("X-Trace-Id"))
                .isEqualTo("req-abc");
    }

    @Test
    @DisplayName("後続チェーンの Reactor Context から traceId を参照できる")
    void filter_writesTraceIdToReactorContext() {
        MockServerWebExchange exchange = newExchange("req-abc");
        AtomicReference<Object> fromContext = new AtomicReference<>();
        GatewayFilterChain chain = ex -> Mono.deferContextual(ctx -> {
            fromContext.set(ctx.getOrDefault("traceId", null));
            return Mono.empty();
        });

        filter.filter(exchange, chain).block();

        assertThat(fromContext.get()).isEqualTo("req-abc");
    }

    @Test
    @DisplayName("traceId を exchange 属性にも保存する（フィルタ外のエラー処理用）")
    void filter_storesTraceIdAsExchangeAttribute() {
        MockServerWebExchange exchange = newExchange("req-abc");

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertThat((String) exchange.getAttribute("traceId")).isEqualTo("req-abc");
    }

    @Test
    @DisplayName("完了後に MDC の traceId が除去される")
    void filter_clearsMdcAfterCompletion() {
        MockServerWebExchange exchange = newExchange("req-abc");

        filter.filter(exchange, ex -> Mono.empty()).block();

        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("後続チェーンがエラーでも MDC の traceId が除去される")
    void filter_clearsMdcOnError() {
        MockServerWebExchange exchange = newExchange("req-abc");

        filter.filter(exchange, ex -> Mono.error(new IllegalStateException("x")))
                .onErrorResume(e -> Mono.empty()).block();

        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("フィルタ順序は最高優先度である")
    void getOrder_isHighestPrecedence() {
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }
}
