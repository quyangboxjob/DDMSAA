package com.example.ddmsaa.infrastructure.rest;

import com.example.ddmsaa.common.exception.BusinessException;
import com.example.ddmsaa.infrastructure.jwt.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * サービス間 HTTP 呼び出し用の {@link ServiceClient} を生成するファクトリ。
 *
 * <p>base-url は {@code app.services.<serviceName>.base-url}、タイムアウトは
 * {@code app.services.connect-timeout-seconds}（既定 5）・
 * {@code app.services.read-timeout-seconds}（既定 30）から解決する。受信中の HTTP リクエストに
 * Bearer の Authorization があれば、そのまま下流へ転送する（下流も JWT を自分で検証する。
 * X-User-* ヘッダーは信用されない）。認証済みスレッドでは X-User-Id / X-User-Role（参考情報）を、
 * 常に X-Request-Id（MDC の traceId、無ければ UUID）を付与する。</p>
 */
@Component
public class RestClientFactory {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String DEFAULT_CLIENT_ERROR_MESSAGE_ID = "E0008";

    private final Environment environment;

    /**
     * ファクトリを生成する。
     *
     * @param environment 設定値の解決に使う Environment
     */
    public RestClientFactory(Environment environment) {
        this.environment = environment;
    }

    /**
     * 指定サービス向けのクライアントを生成する。
     *
     * @param serviceName サービス名（{@code app.services.<serviceName>.base-url} のキー部分）
     * @return 生成したクライアント
     * @throws IllegalStateException serviceName が app.services.* に未登録の場合
     */
    public ServiceClient create(String serviceName) {
        String baseUrl = environment.getProperty("app.services." + serviceName + ".base-url");
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Service is not registered: app.services." + serviceName + ".base-url");
        }
        int connectSeconds = environment.getProperty(
                "app.services.connect-timeout-seconds", Integer.class, 5);
        int readSeconds = environment.getProperty(
                "app.services.read-timeout-seconds", Integer.class, 30);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(readSeconds));
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String userId = UserContext.getUserId();
                    if (userId != null) {
                        request.getHeaders().set("X-User-Id", userId);
                        String role = UserContext.getRole();
                        if (role != null) {
                            request.getHeaders().set("X-User-Role", role);
                        }
                    }
                    String authorization = currentBearerAuthorization();
                    if (authorization != null) {
                        request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                    String traceId = MDC.get("traceId");
                    if (traceId == null || traceId.isBlank()) {
                        traceId = UUID.randomUUID().toString().replace("-", "");
                    }
                    request.getHeaders().set("X-Request-Id", traceId);
                    return execution.execute(request, body);
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw toBusinessException(response.getBody().readAllBytes());
                })
                .build();
        return new RestServiceClient(restClient);
    }

    /**
     * 受信中の HTTP リクエストの Bearer Authorization を返す。リクエストスレッド以外（バッチ等）や
     * Bearer でない場合は null。
     *
     * @return Authorization ヘッダーの値。無ければ null
     */
    private static String currentBearerAuthorization() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String value = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (value != null && value.startsWith("Bearer ")) {
                return value;
            }
        }
        return null;
    }

    private static BusinessException toBusinessException(byte[] body) {
        String messageId = DEFAULT_CLIENT_ERROR_MESSAGE_ID;
        List<String> args = new ArrayList<>();
        try {
            JsonNode node = MAPPER.readTree(body);
            if (node != null && node.hasNonNull("messageId")) {
                messageId = node.get("messageId").asText();
                JsonNode messageArgs = node.get("messageArgs");
                if (messageArgs != null && messageArgs.isArray()) {
                    messageArgs.forEach(a -> args.add(a.asText()));
                }
            }
        } catch (IOException e) {
            messageId = DEFAULT_CLIENT_ERROR_MESSAGE_ID;
        }
        return new BusinessException(messageId, args.toArray(new String[0]));
    }

    private static boolean isConnectFailure(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof ConnectException || t instanceof HttpConnectTimeoutException) {
                return true;
            }
            if (t instanceof SocketTimeoutException || t instanceof HttpTimeoutException) {
                String message = t.getMessage();
                return message != null && message.toLowerCase().contains("connect");
            }
        }
        return false;
    }

    private static <T> T translate(Supplier<T> call) {
        try {
            return call.get();
        } catch (ResourceAccessException e) {
            if (isConnectFailure(e)) {
                throw new BusinessException("E0013");
            }
            throw new BusinessException("E0043");
        }
    }

    /** RestClient を使う {@link ServiceClient} 実装。 */
    private static final class RestServiceClient implements ServiceClient {

        private final RestClient client;

        private RestServiceClient(RestClient client) {
            this.client = client;
        }

        @Override
        public <T> T get(String path, Class<T> responseType) {
            return translate(() -> client.get().uri(path).retrieve().body(responseType));
        }

        @Override
        public <T> T post(String path, Object body, Class<T> responseType) {
            return translate(() -> exchange(client.post().uri(path), body, responseType));
        }

        @Override
        public <T> T put(String path, Object body, Class<T> responseType) {
            return translate(() -> exchange(client.put().uri(path), body, responseType));
        }

        @Override
        public void delete(String path) {
            translate(() -> client.delete().uri(path).retrieve().toBodilessEntity());
        }

        private static <T> T exchange(RestClient.RequestBodySpec spec, Object body,
                Class<T> responseType) {
            RestClient.RequestBodySpec withBody = body == null ? spec : spec.body(body);
            return withBody.retrieve().body(responseType);
        }
    }
}
