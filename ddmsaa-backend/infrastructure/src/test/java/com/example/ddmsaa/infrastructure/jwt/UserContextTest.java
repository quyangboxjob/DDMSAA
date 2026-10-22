package com.example.ddmsaa.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserContext のスレッド単位のログイン者保持")
class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("set した値を getUserId / getRole で取得できる")
    void returnsValuesAfterSet() {
        UserContext.set("A0001", "ADMIN");

        assertThat(UserContext.getUserId()).isEqualTo("A0001");
        assertThat(UserContext.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("未設定なら null を返し例外にならない")
    void returnsNullWhenNotSet() {
        assertThat(UserContext.getUserId()).isNull();
        assertThat(UserContext.getRole()).isNull();
    }

    @Test
    @DisplayName("clear で値が消え、未設定での clear も例外にならない")
    void clearIsIdempotent() {
        UserContext.set("E0001", "EMPLOYEE");

        UserContext.clear();
        UserContext.clear();

        assertThat(UserContext.getUserId()).isNull();
        assertThat(UserContext.getRole()).isNull();
    }

    @Test
    @DisplayName("別スレッドには値が見えない")
    void isolatedPerThread() throws Exception {
        UserContext.set("C0001", "CUSTOMER");
        AtomicReference<String> seen = new AtomicReference<>("unset");

        Thread thread = new Thread(() -> seen.set(UserContext.getUserId()));
        thread.start();
        thread.join();

        assertThat(seen.get()).isNull();
    }
}
