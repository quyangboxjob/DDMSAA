package com.example.ddmsaa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddmsaa.common.response.FieldError;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvalidRequestException の保持値")
class InvalidRequestExceptionTest {

    @Test
    @DisplayName("field と reason のみの場合は既定の messageId E0008 と 1 件の details を持つ")
    void defaultMessageId() {
        InvalidRequestException ex = new InvalidRequestException("role", "不正");

        assertThat(ex.getMessageId()).isEqualTo("E0008");
        assertThat(ex.getDetails()).containsExactly(new FieldError("role", "不正"));
    }

    @Test
    @DisplayName("messageId を指定できる")
    void explicitMessageId() {
        InvalidRequestException ex = new InvalidRequestException("E0053", "file", "空");

        assertThat(ex.getMessageId()).isEqualTo("E0053");
        assertThat(ex.getDetails()).containsExactly(new FieldError("file", "空"));
    }

    @Test
    @DisplayName("details を直接渡すと E0008 で複数件を保持する")
    void detailsConstructor() {
        List<FieldError> details = List.of(new FieldError("a", "x"), new FieldError("b", "y"));

        InvalidRequestException ex = new InvalidRequestException(details);

        assertThat(ex.getMessageId()).isEqualTo("E0008");
        assertThat(ex.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("details が null なら空リストになる")
    void nullDetails() {
        assertThat(new InvalidRequestException((List<FieldError>) null).getDetails()).isEmpty();
    }

    @Test
    @DisplayName("BusinessException を継承しない")
    void isNotBusinessException() {
        assertThat(new InvalidRequestException("a", "b"))
                .isNotInstanceOf(BusinessException.class)
                .isInstanceOf(RuntimeException.class);
    }
}
