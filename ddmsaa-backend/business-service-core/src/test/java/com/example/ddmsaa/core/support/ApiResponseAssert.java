package com.example.ddmsaa.core.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import org.assertj.core.api.AbstractAssert;
import org.springframework.test.web.servlet.MvcResult;

/**
 * {@code Result} / {@code ErrorResponse} 包絡の AssertJ アサーション（spec-07 の契約）。
 *
 * <pre>
 * ApiResponseAssert.assertThat(result).isSuccess().hasMessageId("I0010").dataIs("OD00000001");
 * ApiResponseAssert.assertThat(result).isError(409, "E0006");
 * </pre>
 */
public class ApiResponseAssert extends AbstractAssert<ApiResponseAssert, MvcResult> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonNode body;

    private ApiResponseAssert(MvcResult actual) {
        super(actual, ApiResponseAssert.class);
        try {
            String text = actual.getResponse()
                    .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
            this.body = text.isEmpty() ? MAPPER.nullNode() : MAPPER.readTree(text);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new AssertionError("response body is not JSON: " + e.getOriginalMessage(), e);
        }
    }

    /**
     * 応答のアサーションを開始する。
     *
     * @param actual MockMvc の結果
     * @return アサーション
     */
    public static ApiResponseAssert assertThat(MvcResult actual) {
        return new ApiResponseAssert(actual);
    }

    /**
     * HTTP 200 で、{@code Result.code} が {@code "0"} であることを検証する。
     *
     * @return this
     */
    public ApiResponseAssert isSuccess() {
        isNotNull();
        int status = actual.getResponse().getStatus();
        if (status != 200) {
            failWithMessage("expected HTTP 200 but was %d. body=%s", status, body);
        }
        if (!"0".equals(body.path("code").asText())) {
            failWithMessage("expected Result.code \"0\" but body=%s", body);
        }
        return this;
    }

    /**
     * HTTP ステータスと {@code ErrorResponse.messageId} を検証する。
     *
     * @param status 期待する HTTP ステータス
     * @param messageId 期待する messageId（E 区分）
     * @return this
     */
    public ApiResponseAssert isError(int status, String messageId) {
        isNotNull();
        int actualStatus = actual.getResponse().getStatus();
        if (actualStatus != status) {
            failWithMessage("expected HTTP %d but was %d. body=%s", status, actualStatus, body);
        }
        if (!messageId.equals(body.path("messageId").asText())) {
            failWithMessage("expected messageId <%s> but body=%s", messageId, body);
        }
        return this;
    }

    /**
     * 成功応答の通知用 messageId（I / W）を検証する。
     *
     * @param messageId 期待する messageId
     * @return this
     */
    public ApiResponseAssert hasMessageId(String messageId) {
        isNotNull();
        if (!messageId.equals(body.path("messageId").asText())) {
            failWithMessage("expected messageId <%s> but body=%s", messageId, body);
        }
        return this;
    }

    /**
     * 項目単位のエラー（{@code details[].field}）に指定の項目があることを検証する。
     *
     * @param field 項目名
     * @return this
     */
    public ApiResponseAssert hasFieldError(String field) {
        isNotNull();
        for (JsonNode detail : body.path("details")) {
            if (field.equals(detail.path("field").asText())) {
                return this;
            }
        }
        failWithMessage("expected a details[] entry for field <%s> but body=%s", field, body);
        return this;
    }

    /**
     * {@code data} を文字列（またはスカラ値）として検証する。
     *
     * @param expected 期待する値
     * @return this
     */
    public ApiResponseAssert dataIs(String expected) {
        isNotNull();
        if (!expected.equals(body.path("data").asText())) {
            failWithMessage("expected data <%s> but body=%s", expected, body);
        }
        return this;
    }

    /**
     * 応答本文の JSON を返す（独自の検証用）。
     *
     * @return JSON
     */
    public JsonNode body() {
        return body;
    }

    /**
     * {@code data} の JSON を返す（独自の検証用）。
     *
     * @return {@code data} ノード（なければ missing node）
     */
    public JsonNode data() {
        return body.path("data");
    }
}
