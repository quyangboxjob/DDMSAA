package com.example.ddmsaa.infrastructure.auth;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis で Magic Link トークンとログイン失敗カウント・ロック状態を管理する認証一時状態ストア。
 *
 * <p>Magic Link は GETDEL で一回使い切りとする。同一ユーザーの再送時は逆引きキー
 * {@code auth:magic-link:user:{userId}} で旧トークンを失効させる。
 * 失敗カウント／ロックの API は設計上保持しているが、DDMSAA の認証フローからは呼び出さない
 * （BR-AUTH-005）。</p>
 *
 * <p>Redis を使わないサービス（business-service-core）が infrastructure をスキャンしても起動が失敗
 * しないよう、Redis クライアントが classpath にあるときだけ Bean 化する。</p>
 */
@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
public class AuthStateStore {

    private static final String TOKEN_KEY_PREFIX = "auth:magic-link:";

    private static final String USER_KEY_PREFIX = "auth:magic-link:user:";

    private static final String FAILURE_KEY_PREFIX = "auth:login-failure:";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.auth.max-login-failure-count:5}")
    private int maxLoginFailureCount = 5;

    @Value("${app.auth.login-lockout-seconds:1800}")
    private int loginLockoutSeconds = 1800;

    /**
     * ストアを生成する。
     *
     * @param redisTemplate Redis 文字列テンプレート
     */
    public AuthStateStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Magic Link トークンを保存する。
     *
     * <p>同じ userId の旧トークンが逆引きキーにあれば、その旧トークンのキーを削除して失効させる。
     * その後、新トークンと逆引きキーを同じ TTL で保存する。既存の同一トークンは上書きする。</p>
     *
     * @param token Magic Link トークン
     * @param userId 対象ユーザー ID
     * @param ttlSeconds 有効期間（秒、正の値）
     * @throws IllegalArgumentException token / userId が null、または ttlSeconds が 0 以下の場合
     */
    public void saveMagicLinkToken(String token, String userId, int ttlSeconds) {
        if (token == null || userId == null || ttlSeconds <= 0) {
            throw new IllegalArgumentException(
                    "token, userId and positive ttlSeconds are required");
        }
        String userKey = USER_KEY_PREFIX + userId;
        String oldToken = redisTemplate.opsForValue().get(userKey);
        if (oldToken != null && !oldToken.equals(token)) {
            redisTemplate.delete(TOKEN_KEY_PREFIX + oldToken);
        }
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, userId, ttl);
        redisTemplate.opsForValue().set(userKey, token, ttl);
    }

    /**
     * Magic Link トークンを一回だけ消費する（GETDEL、原子的）。
     *
     * <p>成功時は逆引きキーも削除する（値が当該トークンのときだけ）。</p>
     *
     * @param token Magic Link トークン
     * @return 対応するユーザー ID。2 回目以降・期限切れ・token が null の場合は null
     */
    public String consumeMagicLinkToken(String token) {
        if (token == null) {
            return null;
        }
        String userId = redisTemplate.opsForValue().getAndDelete(TOKEN_KEY_PREFIX + token);
        if (userId != null) {
            String userKey = USER_KEY_PREFIX + userId;
            if (token.equals(redisTemplate.opsForValue().get(userKey))) {
                redisTemplate.delete(userKey);
            }
        }
        return userId;
    }

    /**
     * ログイン失敗カウントを 1 加算し、TTL を {@code app.auth.login-lockout-seconds} に更新する。
     *
     * @param userId ユーザー ID
     * @return 加算後のカウント
     */
    public int incrementLoginFailure(String userId) {
        String key = FAILURE_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(loginLockoutSeconds));
        return count == null ? 0 : count.intValue();
    }

    /**
     * ユーザーがロック中か判定する。
     *
     * @param userId ユーザー ID
     * @return カウントが {@code app.auth.max-login-failure-count} 以上なら true。
     *         閾値未満またはキー無しなら false
     */
    public boolean isLocked(String userId) {
        String value = redisTemplate.opsForValue().get(FAILURE_KEY_PREFIX + userId);
        if (value == null) {
            return false;
        }
        try {
            return Integer.parseInt(value) >= maxLoginFailureCount;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * ログイン失敗カウントを消去する。キーが無くても例外にならない（冪等）。
     *
     * @param userId ユーザー ID
     */
    public void clearLoginFailure(String userId) {
        redisTemplate.delete(FAILURE_KEY_PREFIX + userId);
    }
}
