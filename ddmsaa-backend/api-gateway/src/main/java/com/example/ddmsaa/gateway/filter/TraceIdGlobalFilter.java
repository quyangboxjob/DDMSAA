package com.example.ddmsaa.gateway.filter;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * リクエストごとの traceId を確定し、MDC・下流リクエスト・応答ヘッダへ貫通させる GlobalFilter。
 *
 * <p>受信した {@code X-Request-Id} を採用し、無い場合は UUID（ハイフン除去・32 文字）を生成する。
 * リアクティブスレッド間の伝播のため Reactor Context にも {@code traceId} を書き込む
 * （{@code GatewayApplication} の自動コンテキスト伝播の有効化と併用）。
 */
@Component
public class TraceIdGlobalFilter implements GlobalFilter, Ordered {

    /** 受信・下流透過するリクエスト ID ヘッダ。 */
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    /** 応答に付与する traceId ヘッダ。 */
    private static final String HEADER_TRACE_ID = "X-Trace-Id";

    /** MDC / Reactor Context のキー。 */
    private static final String MDC_KEY = "traceId";

    /** 既定のコンストラクタ。 */
    public TraceIdGlobalFilter() {
        // 状態を持たない
    }

    /**
     * traceId を確定して後続フィルタへ渡す。
     *
     * <p>応答ヘッダ {@code X-Trace-Id} は、コミット済みでは設定できないため
     * {@code beforeCommit} コールバックで設定する。
     *
     * @param exchange 現在の exchange
     * @param chain 後続のフィルタチェーン
     * @return 後続処理の完了を表す {@link Mono}
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        final String resolvedTraceId = traceId;

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> headers.set(HEADER_REQUEST_ID, resolvedTraceId))
                .build();
        ServerWebExchange mutated = exchange.mutate().request(request).build();

        ServerHttpResponse response = mutated.getResponse();
        response.beforeCommit(() -> {
            response.getHeaders().set(HEADER_TRACE_ID, resolvedTraceId);
            return Mono.empty();
        });

        exchange.getAttributes().put(MDC_KEY, resolvedTraceId);
        MDC.put(MDC_KEY, resolvedTraceId);
        return chain.filter(mutated)
                .contextWrite(Context.of(MDC_KEY, resolvedTraceId))
                .doFinally(signal -> MDC.remove(MDC_KEY));
    }

    /**
     * フィルタ順序（最高優先度）を返す。
     *
     * @return {@link Ordered#HIGHEST_PRECEDENCE}
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
