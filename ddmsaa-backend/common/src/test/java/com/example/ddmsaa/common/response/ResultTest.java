package com.example.ddmsaa.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

@DisplayName("Result の生成と JSON 出力")
class ResultTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("ok(data) は code 0・data・MDC の traceId・timestamp を持つ")
    void okWithData() {
        MDC.put("traceId", "t-1");

        Result<String> result = Result.ok("v");

        assertThat(result.code()).isEqualTo("0");
        assertThat(result.data()).isEqualTo("v");
        assertThat(result.traceId()).isEqualTo("t-1");
        assertThat(result.timestamp()).isNotNull();
        assertThat(result.messageId()).isNull();
        assertThat(result.messageArgs()).isNull();
    }

    @Test
    @DisplayName("ok(data, messageId, args) は messageId と messageArgs を持つ")
    void okWithMessage() {
        Result<Integer> result = Result.ok(1, "I0010", "U0001");

        assertThat(result.messageId()).isEqualTo("I0010");
        assertThat(result.messageArgs()).containsExactly("U0001");
        assertThat(result.data()).isEqualTo(1);
    }

    @Test
    @DisplayName("args が空なら messageArgs は null になる")
    void okWithMessageNoArgs() {
        Result<Void> result = Result.ok(null, "I0011");

        assertThat(result.messageId()).isEqualTo("I0011");
        assertThat(result.messageArgs()).isNull();
    }

    @Test
    @DisplayName("traceId が MDC に無ければ null")
    void noTraceId() {
        assertThat(Result.ok("v").traceId()).isNull();
    }

    @Test
    @DisplayName("null のフィールドは JSON から省略される")
    void nullFieldsOmittedInJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        JsonNode json = mapper.readTree(mapper.writeValueAsString(Result.ok(null)));

        assertThat(json.get("code").isTextual()).isTrue();
        assertThat(json.get("code").asText()).isEqualTo("0");
        assertThat(json.has("message")).isTrue();
        assertThat(json.has("data")).isFalse();
        assertThat(json.has("messageId")).isFalse();
        assertThat(json.has("messageArgs")).isFalse();
        assertThat(json.has("traceId")).isFalse();
    }
}
