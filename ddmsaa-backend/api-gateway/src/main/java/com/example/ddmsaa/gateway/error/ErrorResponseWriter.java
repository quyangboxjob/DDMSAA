package com.example.ddmsaa.gateway.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway のエラー応答を統一 JSON として {@link ServerHttpResponse} に書き出す共有コンポーネント。
 *
 * <p>応答ボディは {@code messageId} / {@code timestamp} / {@code path} / {@code traceId} の
 * 4 項目。{@link ObjectMapper} で直接組み立て、common モジュールの {@code ErrorResponse} には
 * 依存しない。
 */
@Component
public class ErrorResponseWriter {

    /** 応答 timestamp のオフセット（JST, +09:00）。 */
    private static final ZoneOffset TIMESTAMP_OFFSET = ZoneOffset.ofHours(9);

    /** JSON シリアライザ。 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 既定のコンストラクタ。 */
    public ErrorResponseWriter() {
        // 状態は objectMapper のみ（フィールド初期化済み）
    }

    /**
     * エラー応答を書き出す。
     *
     * <p>応答がすでにコミット済みの場合は何も書かずに完了する。
     *
     * @param exchange 現在の exchange（{@code path} の取得元・書き込み先）
     * @param status HTTP ステータス
     * @param messageId メッセージ ID（例: {@code E0040}）
     * @param traceId トレース ID（取得できない場合は {@code null}）
     * @return 書き込み完了を表す {@link Mono}
     */
    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String messageId,
            String traceId) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messageId", messageId);
        body.put("timestamp", DateTimeFormatter.ISO_OFFSET_DATE_TIME
                .format(OffsetDateTime.now(TIMESTAMP_OFFSET)));
        body.put("path", exchange.getRequest().getPath().value());
        body.put("traceId", traceId);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"messageId\":\"" + messageId + "\"}").getBytes(StandardCharsets.UTF_8);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setContentLength(bytes.length);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
