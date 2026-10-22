package com.example.ddmsaa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuthException の保持値")
class AuthExceptionTest {

    @Test
    @DisplayName("messageId と args を保持する")
    void holdsMessageIdAndArgs() {
        AuthException ex = new AuthException("E0004", "a", "b");

        assertThat(ex.getMessageId()).isEqualTo("E0004");
        assertThat(ex.getArgs()).containsExactly("a", "b");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("args 省略時は空配列になる")
    void argsDefaultsToEmpty() {
        assertThat(new AuthException("E0004").getArgs()).isEmpty();
    }
}
