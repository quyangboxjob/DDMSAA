package com.example.ddmsaa.common.web.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.ServletException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("TraceIdFilter のトレース ID 付与")
class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("X-Request-Id があればそれを MDC と X-Trace-Id に使う")
    void usesRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "req-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> inChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> inChain.set(MDC.get("traceId")));

        assertThat(inChain.get()).isEqualTo("req-123");
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("req-123");
    }

    @Test
    @DisplayName("X-Request-Id が無ければハイフン無し UUID を生成する")
    void generatesWhenMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> inChain = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest(), response,
                (req, res) -> inChain.set(MDC.get("traceId")));

        assertThat(inChain.get()).matches("[0-9a-f]{32}");
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo(inChain.get());
    }

    @Test
    @DisplayName("X-Request-Id が空白なら生成する")
    void generatesWhenBlank() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader("X-Trace-Id")).matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("処理後は MDC がクリアされる")
    void clearsMdcAfterChain() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                new MockFilterChain());

        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("チェーンが例外を投げても MDC がクリアされる")
    void clearsMdcOnException() {
        assertThatThrownBy(() -> filter.doFilter(new MockHttpServletRequest(),
                new MockHttpServletResponse(), (req, res) -> {
                    throw new ServletException("boom");
                })).isInstanceOf(ServletException.class);

        assertThat(MDC.get("traceId")).isNull();
    }
}
