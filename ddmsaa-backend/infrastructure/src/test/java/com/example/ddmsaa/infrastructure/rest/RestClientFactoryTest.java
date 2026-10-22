package com.example.ddmsaa.infrastructure.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ddmsaa.common.exception.BusinessException;
import com.example.ddmsaa.infrastructure.jwt.UserContext;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@DisplayName("RestClientFactory のサービス間呼び出し")
class RestClientFactoryTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        UserContext.clear();
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    private MockEnvironment startServer(AtomicReference<com.sun.net.httpserver.Headers> headers,
            int status, String body, int readTimeoutSeconds) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            if (headers != null) {
                headers.set(exchange.getRequestHeaders());
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return new MockEnvironment()
                .withProperty("app.services.core.base-url",
                        "http://127.0.0.1:" + server.getAddress().getPort())
                .withProperty("app.services.read-timeout-seconds",
                        String.valueOf(readTimeoutSeconds));
    }

    @Test
    @DisplayName("認証済みスレッドでは X-User-Id / X-User-Role と X-Request-Id を付与する")
    void addsIdentityAndTraceHeaders() throws Exception {
        AtomicReference<com.sun.net.httpserver.Headers> headers = new AtomicReference<>();
        MockEnvironment env = startServer(headers, 200, "ok", 5);
        UserContext.set("A0001", "ADMIN");
        MDC.put("traceId", "trace-1");

        String result = new RestClientFactory(env).create("core").get("/ping", String.class);

        assertThat(result).isEqualTo("ok");
        assertThat(headers.get().getFirst("X-User-Id")).isEqualTo("A0001");
        assertThat(headers.get().getFirst("X-User-Role")).isEqualTo("ADMIN");
        assertThat(headers.get().getFirst("X-Request-Id")).isEqualTo("trace-1");
    }

    @Test
    @DisplayName("未認証スレッドでは識別ヘッダーを付けず、traceId が無ければ UUID を生成する")
    void anonymousGeneratesRequestId() throws Exception {
        AtomicReference<com.sun.net.httpserver.Headers> headers = new AtomicReference<>();
        MockEnvironment env = startServer(headers, 200, "ok", 5);

        new RestClientFactory(env).create("core").get("/ping", String.class);

        assertThat(headers.get().getFirst("X-User-Id")).isNull();
        assertThat(headers.get().getFirst("X-User-Role")).isNull();
        assertThat(headers.get().getFirst("X-Request-Id")).matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("受信中のリクエストの Bearer Authorization を下流へそのまま転送する")
    void forwardsBearerAuthorization() throws Exception {
        AtomicReference<com.sun.net.httpserver.Headers> headers = new AtomicReference<>();
        MockEnvironment env = startServer(headers, 200, "ok", 5);
        MockHttpServletRequest incoming = new MockHttpServletRequest();
        incoming.addHeader("Authorization", "Bearer jwt-from-caller");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incoming));

        new RestClientFactory(env).create("core").get("/ping", String.class);

        assertThat(headers.get().getFirst("Authorization")).isEqualTo("Bearer jwt-from-caller");
    }

    @Test
    @DisplayName("リクエストスレッド以外、または Bearer でない場合は Authorization を付けない")
    void doesNotForwardWithoutBearer() throws Exception {
        AtomicReference<com.sun.net.httpserver.Headers> headers = new AtomicReference<>();
        MockEnvironment env = startServer(headers, 200, "ok", 5);
        RestClientFactory factory = new RestClientFactory(env);

        factory.create("core").get("/ping", String.class);
        assertThat(headers.get().getFirst("Authorization")).isNull();

        MockHttpServletRequest basic = new MockHttpServletRequest();
        basic.addHeader("Authorization", "Basic abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(basic));
        factory.create("core").get("/ping", String.class);
        assertThat(headers.get().getFirst("Authorization")).isNull();
    }

    @Test
    @DisplayName("app.services.* に未登録のサービス名は IllegalStateException")
    void unknownServiceFailsFast() {
        RestClientFactory factory = new RestClientFactory(new MockEnvironment());

        assertThatThrownBy(() -> factory.create("unknown"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("4xx 応答は応答の messageId を持つ BusinessException にラップする")
    void clientErrorIsWrapped() throws Exception {
        MockEnvironment env = startServer(null, 404, "{\"messageId\":\"E0005\"}", 5);

        assertThatThrownBy(() -> new RestClientFactory(env).create("core")
                .post("/x", "body", String.class))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0005"));
    }

    @Test
    @DisplayName("接続できなければ E0013")
    void connectFailureIsE0013() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        MockEnvironment env = new MockEnvironment()
                .withProperty("app.services.core.base-url", "http://127.0.0.1:" + port);

        assertThatThrownBy(() -> new RestClientFactory(env).create("core").delete("/x"))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0013"));
    }

    @Test
    @DisplayName("読み取りタイムアウトは E0043")
    void readTimeoutIsE0043() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.close();
        });
        server.start();
        MockEnvironment env = new MockEnvironment()
                .withProperty("app.services.core.base-url",
                        "http://127.0.0.1:" + server.getAddress().getPort())
                .withProperty("app.services.read-timeout-seconds", "1");

        assertThatThrownBy(() -> new RestClientFactory(env).create("core").get("/slow",
                String.class))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getMessageId()).isEqualTo("E0043"));
    }
}
