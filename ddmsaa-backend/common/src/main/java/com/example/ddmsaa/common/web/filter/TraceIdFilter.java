package com.example.ddmsaa.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * リクエストごとに traceId を MDC へ設定し、応答ヘッダ X-Trace-Id に返すフィルタ。
 *
 * <p>X-Request-Id ヘッダがあればそれを、無ければ UUID を採番して用いる。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /** 受信するリクエスト ID ヘッダ。 */
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** 応答に付与する traceId ヘッダ。 */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC のキー。 */
    private static final String MDC_KEY = "traceId";

    /** {@inheritDoc} */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(REQUEST_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
