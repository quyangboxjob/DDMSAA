package com.example.ddmsaa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PayloadTooLargeException の保持値")
class PayloadTooLargeExceptionTest {

    @Test
    @DisplayName("messageId と args を保持する")
    void holdsMessageIdAndArgs() {
        PayloadTooLargeException ex = new PayloadTooLargeException("E0052", "50");

        assertThat(ex.getMessageId()).isEqualTo("E0052");
        assertThat(ex.getArgs()).containsExactly("50");
    }

    @Test
    @DisplayName("BusinessException を継承しない")
    void isNotBusinessException() {
        assertThat(new PayloadTooLargeException("E0052"))
                .isNotInstanceOf(BusinessException.class)
                .isInstanceOf(RuntimeException.class);
    }
}
