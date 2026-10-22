package com.example.ddmsaa.infrastructure.jwt;

import com.example.ddmsaa.common.exception.AuthException;
import com.example.ddmsaa.common.response.ErrorResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization ヘッダの Bearer トークンを検証し、UserContext と SecurityContextHolder に
 * ログイン者情報を設定するフィルタ。
 *
 * <p>{@code @Component} は付与しない。{@code @Bean} 登録は各サービスの SecurityConfig が行う。
 * UserContext と MDC（userId）は認証の成否に関わらず finally で必ず消去する。</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String MDC_USER_ID = "userId";

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private static final List<String> PERMIT_ALL_PREFIXES = List.of("/api/v1/auth/", "/actuator/");

    private final JwtUtil jwtUtil;

    /**
     * フィルタを生成する。
     *
     * @param jwtUtil トークン検証に使う JwtUtil
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Bearer トークンを検証してログイン者情報を設定し、チェーンを継続する。
     *
     * <p>トークンの検証に失敗した場合は 401 を返し、チェーンを継続しない。
     * Authorization ヘッダが無い／Bearer でない場合は認証を設定せずチェーンを継続する。</p>
     *
     * @param request リクエスト
     * @param response レスポンス
     * @param filterChain フィルタチェーン
     * @throws ServletException 後続フィルタが送出した場合
     * @throws IOException 後続フィルタが送出した場合
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith(BEARER_PREFIX)) {
                Claims claims;
                try {
                    claims = jwtUtil.validateAndGetClaims(header.substring(BEARER_PREFIX.length()));
                } catch (AuthException e) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(request, response, e.getMessageId());
                    return;
                }
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                UserContext.set(userId, role);
                MDC.put(MDC_USER_ID, userId);
                List<SimpleGrantedAuthority> authorities = role == null
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, authorities));
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            MDC.remove(MDC_USER_ID);
        }
    }

    /**
     * 401 と {@link ErrorResponse}（E0001 期限切れ・E0040 未指定・E0041 不正）を書き込む。
     * 本文を返すことで、画面側が messageId で「セッション切れ」と判別できる（spec-12 §3）。
     *
     * @param request リクエスト
     * @param response レスポンス
     * @param messageId エラーメッセージ ID
     * @throws IOException 書き込みに失敗した場合
     */
    private static void writeUnauthorized(HttpServletRequest request,
            HttpServletResponse response, String messageId) throws IOException {
        ErrorResponse body = ErrorResponse.of(messageId, request.getRequestURI(),
                MDC.get("traceId"));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(response.getWriter(), body);
    }

    /**
     * permitAll パス（{@code /api/v1/auth/**}、{@code /actuator/**}）ではトークン検証を行わない。
     *
     * @param request リクエスト
     * @return 検証を行わない場合 true
     * @throws ServletException 判定に失敗した場合
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path != null && PERMIT_ALL_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
