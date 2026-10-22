package com.example.ddmsaa.gateway.error;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全リアクティブ例外を統一 JSON 形式へ変換して返すエラーハンドラ。
 *
 * <p>{@code ResponseStatusExceptionHandler} は継承しない（スーパークラスの書き込みが
 * {@link ErrorResponseWriter} と競合してボディが壊れるため）。
 * 下流サービスが返したエラー応答は例外にならないため、そのまま透過され二重ラップされない。
 */
@Component
@Order(-1)
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

    /** Reactor Context / MDC に格納される traceId のキー。 */
    private static final String TRACE_ID_KEY = "traceId";

    /** 原因チェーンを辿る最大深さ（循環参照対策）。 */
    private static final int MAX_CAUSE_DEPTH = 10;

    /** エラー応答の書き出し先。 */
    private final ErrorResponseWriter errorResponseWriter;

    /**
     * 依存コンポーネントを受け取る。
     *
     * @param errorResponseWriter エラー応答ライター
     */
    public GatewayErrorHandler(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    /**
     * 例外を HTTP ステータスとメッセージ ID に変換して応答する。
     *
     * <p>応答がコミット済みの場合は書き込めないため、例外をそのまま再送出する。
     *
     * @param exchange 現在の exchange
     * @param ex 発生した例外
     * @return 応答書き込み完了を表す {@link Mono}
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        return Mono.deferContextual(ctx -> {
            String traceId = resolveTraceId(exchange, ctx.getOrDefault(TRACE_ID_KEY,
                    MDC.get(TRACE_ID_KEY)));
            HttpStatus status = determineStatus(ex);
            String messageId = determineMessageId(status);
            if (status.is5xxServerError()) {
                log.error("Gateway error: status={} messageId={} path={}", status.value(),
                        messageId, exchange.getRequest().getPath().value(), ex);
            } else {
                log.debug("Gateway error: status={} messageId={} path={}", status.value(),
                        messageId, exchange.getRequest().getPath().value(), ex);
            }
            return errorResponseWriter.write(exchange, status, messageId, traceId);
        });
    }

    /**
     * traceId を決定する。Reactor Context / MDC で得られない場合（フィルタチェーン外で
     * 発生した例外など）は、{@code TraceIdGlobalFilter} が exchange 属性へ保存した値、
     * 受信 {@code X-Request-Id} の順に補う。
     *
     * @param exchange 現在の exchange
     * @param fromContext Reactor Context または MDC から得た値（無ければ {@code null}）
     * @return traceId（どこからも得られなければ {@code null}）
     */
    private String resolveTraceId(ServerWebExchange exchange, String fromContext) {
        if (fromContext != null) {
            return fromContext;
        }
        Object attribute = exchange.getAttribute(TRACE_ID_KEY);
        if (attribute instanceof String value) {
            return value;
        }
        return exchange.getRequest().getHeaders().getFirst("X-Request-Id");
    }

    /**
     * 例外から応答 HTTP ステータスを決定する。
     *
     * @param ex 発生した例外
     * @return 404 / 403 / 502 / 504 のいずれか、該当しなければ 500
     */
    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            int code = rse.getStatusCode().value();
            if (code == 404 || code == 403 || code == 502 || code == 504) {
                return HttpStatus.valueOf(code);
            }
        }
        Throwable current = ex;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof TimeoutException) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
            if (current instanceof ConnectException || current instanceof UnknownHostException
                    || current instanceof NoRouteToHostException) {
                return HttpStatus.BAD_GATEWAY;
            }
            current = current.getCause();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * HTTP ステータスに対応するメッセージ ID を返す。
     *
     * @param status 決定済みの HTTP ステータス
     * @return メッセージ ID（想定外のステータスは {@code E0012}）
     */
    private String determineMessageId(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "E0042";
            case FORBIDDEN -> "E0002";
            case BAD_GATEWAY -> "E0013";
            case GATEWAY_TIMEOUT -> "E0043";
            default -> "E0012";
        };
    }
}
