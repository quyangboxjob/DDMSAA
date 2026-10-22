package com.example.ddmsaa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BusinessException の保持値")
class BusinessExceptionTest {

    @Test
    @DisplayName("messageId と args を保持する")
    void holdsMessageIdAndArgs() {
        BusinessException ex = new BusinessException("E0006", "a", "b");

        assertThat(ex.getMessageId()).isEqualTo("E0006");
        assertThat(ex.getArgs()).containsExactly("a", "b");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("args 省略時は空配列になる")
    void argsDefaultsToEmpty() {
        assertThat(new BusinessException("E0006").getArgs()).isEmpty();
    }
}
