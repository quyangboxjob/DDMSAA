package com.example.ddmsaa.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("AuthStateStore の Magic Link とログイン失敗管理")
class AuthStateStoreTest {

    private StringRedisTemplate redis;

    private ValueOperations<String, String> ops;

    private AuthStateStore store;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        store = new AuthStateStore(redis);
        ReflectionTestUtils.setField(store, "maxLoginFailureCount", 5);
        ReflectionTestUtils.setField(store, "loginLockoutSeconds", 1800);
    }

    @Test
    @DisplayName("初回保存はトークンと逆引きキーを同じ TTL で保存する")
    void saveFirstTime() {
        store.saveMagicLinkToken("tok1", "C0001", 3600);

        verify(ops).set("auth:magic-link:tok1", "C0001", Duration.ofSeconds(3600));
        verify(ops).set("auth:magic-link:user:C0001", "tok1", Duration.ofSeconds(3600));
        verify(redis, never()).delete(any(String.class));
    }

    @Test
    @DisplayName("再送では旧トークンを削除してから新トークンと逆引きキーを保存する")
    void saveInvalidatesOldToken() {
        when(ops.get("auth:magic-link:user:C0001")).thenReturn("old");

        store.saveMagicLinkToken("new", "C0001", 600);

        InOrder order = inOrder(redis, ops);
        order.verify(redis).delete("auth:magic-link:old");
        order.verify(ops).set("auth:magic-link:new", "C0001", Duration.ofSeconds(600));
        order.verify(ops).set("auth:magic-link:user:C0001", "new", Duration.ofSeconds(600));
    }

    @Test
    @DisplayName("同じトークンの再保存では自分自身を削除せず上書きする")
    void saveSameTokenOverwrites() {
        when(ops.get("auth:magic-link:user:C0001")).thenReturn("same");

        store.saveMagicLinkToken("same", "C0001", 600);

        verify(redis, never()).delete(any(String.class));
        verify(ops).set("auth:magic-link:same", "C0001", Duration.ofSeconds(600));
    }

    @Test
    @DisplayName("引数が不正なら IllegalArgumentException")
    void saveRejectsInvalidArguments() {
        assertThatThrownBy(() -> store.saveMagicLinkToken(null, "C0001", 60))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> store.saveMagicLinkToken("t", null, 60))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> store.saveMagicLinkToken("t", "C0001", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("consume はユーザー ID を返し、値が当該トークンなら逆引きキーも削除する")
    void consumeDeletesReverseKey() {
        when(ops.getAndDelete("auth:magic-link:tok1")).thenReturn("C0001");
        when(ops.get("auth:magic-link:user:C0001")).thenReturn("tok1");

        String userId = store.consumeMagicLinkToken("tok1");

        assertThat(userId).isEqualTo("C0001");
        verify(redis).delete("auth:magic-link:user:C0001");
    }

    @Test
    @DisplayName("逆引きキーが別トークンを指していれば削除しない")
    void consumeKeepsReverseKeyOfNewerToken() {
        when(ops.getAndDelete("auth:magic-link:tok1")).thenReturn("C0001");
        when(ops.get("auth:magic-link:user:C0001")).thenReturn("newer");

        assertThat(store.consumeMagicLinkToken("tok1")).isEqualTo("C0001");

        verify(redis, never()).delete("auth:magic-link:user:C0001");
    }

    @Test
    @DisplayName("2 回目以降・期限切れの consume は null を返す")
    void consumeReturnsNullWhenMissing() {
        when(ops.getAndDelete("auth:magic-link:gone")).thenReturn(null);

        assertThat(store.consumeMagicLinkToken("gone")).isNull();
        assertThat(store.consumeMagicLinkToken(null)).isNull();

        verify(redis, never()).delete(any(String.class));
    }

    @Test
    @DisplayName("失敗カウントを加算し TTL を更新して新しい値を返す")
    void incrementLoginFailure() {
        when(ops.increment("auth:login-failure:C0001")).thenReturn(3L);

        int count = store.incrementLoginFailure("C0001");

        assertThat(count).isEqualTo(3);
        verify(redis).expire("auth:login-failure:C0001", Duration.ofSeconds(1800));
    }

    @Test
    @DisplayName("カウントが閾値以上ならロック中、未満またはキー無しなら非ロック")
    void isLocked() {
        when(ops.get("auth:login-failure:A")).thenReturn("5");
        when(ops.get("auth:login-failure:B")).thenReturn("4");
        when(ops.get("auth:login-failure:C")).thenReturn(null);

        assertThat(store.isLocked("A")).isTrue();
        assertThat(store.isLocked("B")).isFalse();
        assertThat(store.isLocked("C")).isFalse();
    }

    @Test
    @DisplayName("clearLoginFailure はキーを削除する")
    void clearLoginFailure() {
        store.clearLoginFailure("C0001");

        verify(redis).delete("auth:login-failure:C0001");
    }
}
