package com.example.ddmsaa.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ErrorResponse と FieldError")
class ErrorResponseTest {

    @Test
    @DisplayName("of は messageId・path・traceId・timestamp を設定する")
    void ofSetsBaseFields() {
        OffsetDateTime before = OffsetDateTime.now();

        ErrorResponse response = ErrorResponse.of("E0008", "/api/v1/users", "trace-1");

        assertThat(response.getMessageId()).isEqualTo("E0008");
        assertThat(response.getPath()).isEqualTo("/api/v1/users");
        assertThat(response.getTraceId()).isEqualTo("trace-1");
        assertThat(response.getTimestamp()).isAfterOrEqualTo(before);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getMessageArgs()).isNull();
        assertThat(response.getDetails()).isNull();
    }

    @Test
    @DisplayName("setter で補完した値を getter で取得できる")
    void settersAndGetters() {
        ErrorResponse response = new ErrorResponse();
        OffsetDateTime now = OffsetDateTime.now();
        List<FieldError> details = List.of(new FieldError("name", "必須"));

        response.setMessageId("E0030");
        response.setMessageArgs(new String[] {"名前"});
        response.setMessage("msg");
        response.setTimestamp(now);
        response.setPath("/p");
        response.setTraceId("t");
        response.setDetails(details);

        assertThat(response.getMessageId()).isEqualTo("E0030");
        assertThat(response.getMessageArgs()).containsExactly("名前");
        assertThat(response.getMessage()).isEqualTo("msg");
        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getPath()).isEqualTo("/p");
        assertThat(response.getTraceId()).isEqualTo("t");
        assertThat(response.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("FieldError は field と reason を record アクセサで返す")
    void fieldError() {
        FieldError error = new FieldError("email", "形式不正");

        assertThat(error.field()).isEqualTo("email");
        assertThat(error.reason()).isEqualTo("形式不正");
        assertThat(error).isEqualTo(new FieldError("email", "形式不正"));
    }
}
