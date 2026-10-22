package com.example.ddmsaa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EntityNotFoundException の保持値")
class EntityNotFoundExceptionTest {

    @Test
    @DisplayName("デフォルトコンストラクタは既定の messageId E0005 と空の args を持つ")
    void defaultConstructorUsesE0005() {
        EntityNotFoundException ex = new EntityNotFoundException();

        assertThat(ex.getMessageId()).isEqualTo("E0005");
        assertThat(ex.getArgs()).isEmpty();
    }

    @Test
    @DisplayName("messageId と args を保持する")
    void holdsMessageIdAndArgs() {
        EntityNotFoundException ex = new EntityNotFoundException("E0099", "x");

        assertThat(ex.getMessageId()).isEqualTo("E0099");
        assertThat(ex.getArgs()).containsExactly("x");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
