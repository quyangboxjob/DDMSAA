package com.example.ddmsaa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ddmsaa.common.message.MessageCatalog;
import com.example.ddmsaa.common.message.YamlMessageCatalog;
import com.example.ddmsaa.common.response.ErrorResponse;
import com.example.ddmsaa.common.response.FieldError;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@DisplayName("GlobalExceptionHandler の例外 → HTTP 変換")
class GlobalExceptionHandlerTest {

    private static final String PATH = "/api/v1/test";

    /** テスト用カタログ。messageId に引数を連結した文字列を返し、"E9999" だけ未登録扱いにする。 */
    private static final MessageCatalog FAKE_CATALOG = new MessageCatalog() {
        @Override
        public String resolve(String messageId, Object... args) {
            if ("E9999".equals(messageId)) {
                return messageId;
            }
            String suffix = args == null || args.length == 0 ? "" : "|" + String.join(",",
                    java.util.Arrays.stream(args).map(String::valueOf).toList());
            return "msg:" + messageId + suffix;
        }

        @Override
        public boolean exists(String messageId) {
            return !"E9999".equals(messageId);
        }
    };

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(FAKE_CATALOG);

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("GET", PATH);
        MDC.put("traceId", "trace-9");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    private static void assertBase(ResponseEntity<ErrorResponse> response, int status,
            String messageId) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessageId()).isEqualTo(messageId);
        assertThat(body.getPath()).isEqualTo(PATH);
        assertThat(body.getTraceId()).isEqualTo("trace-9");
        assertThat(body.getTimestamp()).isNotNull();
        assertThat(body.getMessage()).startsWith("msg:" + messageId);
    }

    @Test
    @DisplayName("MethodArgumentNotValidException は 400 E0008 で項目エラーを details に設定する")
    void methodArgumentNotValid() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "dto");
        binding.addError(new org.springframework.validation.FieldError("dto", "email", "必須です"));
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("sample", String.class), 0);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(
                new MethodArgumentNotValidException(parameter, binding), request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails()).containsExactly(
                new FieldError("email", "必須です"));
    }

    @SuppressWarnings("unused")
    private void sample(String value) {
    }

    /** Bean Validation 検証用の入力。 */
    static class Input {

        @NotBlank(message = "必須です")
        String name;
    }

    @Test
    @DisplayName("ConstraintViolationException は 400 E0008 で項目エラーを details に設定する")
    void constraintViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ConstraintViolationException ex =
                new ConstraintViolationException(validator.validate(new Input()));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails()).containsExactly(
                new FieldError("name", "必須です"));
    }

    @Test
    @DisplayName("InvalidRequestException は 400 で例外の messageId と details を返す（既定 E0008）")
    void invalidRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(
                new InvalidRequestException("role", "不正"), request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails()).containsExactly(new FieldError("role", "不正"));
        assertThat(response.getBody().getMessageArgs()).isNull();
    }

    @Test
    @DisplayName("InvalidRequestException は指定の messageId（E0053）をそのまま返す")
    void invalidRequestWithMessageId() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidRequest(
                new InvalidRequestException("E0053", "file", "空"), request);

        assertBase(response, 400, "E0053");
    }

    @Test
    @DisplayName("型変換失敗（不正な列挙値など）は 400 E0008 で引数名を details に設定する")
    void typeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "XXX", Integer.class, "role", null, new IllegalArgumentException("secret"));

        ResponseEntity<ErrorResponse> response = handler.handleRequestBinding(ex, request);

        assertBase(response, 400, "E0008");
        List<FieldError> details = response.getBody().getDetails();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).field()).isEqualTo("role");
        assertThat(details.get(0).reason()).doesNotContain("secret");
    }

    @Test
    @DisplayName("JSON 不正は 400 E0008 で、パーサのメッセージを利用者に返さない")
    void notReadableJson() {
        HttpInputMessage input = new MockHttpInputMessage(new byte[0]);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error: Unexpected character ('x')",
                new JsonParseException(null, "Unexpected character"), input);

        ResponseEntity<ErrorResponse> response = handler.handleRequestBinding(ex, request);

        assertBase(response, 400, "E0008");
        List<FieldError> details = response.getBody().getDetails();
        assertThat(details).hasSize(1);
        assertThat(details.get(0).field()).isNull();
        assertThat(details.get(0).reason()).doesNotContain("Unexpected").doesNotContain("JSON");
    }

    @Test
    @DisplayName("ボディの列挙値・日付不正は JSON のプロパティ名を field に設定する")
    void notReadableWithProperty() {
        JsonMappingException cause =
                JsonMappingException.from((JsonParser) null, "bad value");
        cause.prependPath(new JsonMappingException.Reference(Object.class, "status"));
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad", cause,
                new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleRequestBinding(ex, request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails().get(0).field()).isEqualTo("status");
    }

    @Test
    @DisplayName("必須クエリ欠落は 400 E0008 でパラメータ名を設定する")
    void missingParameter() {
        ResponseEntity<ErrorResponse> response = handler.handleRequestBinding(
                new MissingServletRequestParameterException("page", "int"), request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails().get(0).field()).isEqualTo("page");
    }

    @Test
    @DisplayName("multipart の必須パート欠落は 400 E0008 でパート名を設定する")
    void missingPart() {
        ResponseEntity<ErrorResponse> response = handler.handleRequestBinding(
                new MissingServletRequestPartException("file"), request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails().get(0).field()).isEqualTo("file");
    }

    @Test
    @DisplayName("パス変数欠落は 400 E0008 で変数名を設定する")
    void missingPathVariable() throws Exception {
        MethodParameter parameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("sample", String.class), 0);

        ResponseEntity<ErrorResponse> response = handler.handleRequestBinding(
                new MissingPathVariableException("orderId", parameter), request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails().get(0).field()).isEqualTo("orderId");
    }

    @Test
    @DisplayName("AuthException は 401 で例外の messageId と args を返す")
    void auth() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAuth(new AuthException("E0001", "x"), request);

        assertBase(response, 401, "E0001");
        assertThat(response.getBody().getMessageArgs()).containsExactly("x");
        assertThat(response.getBody().getMessage()).isEqualTo("msg:E0001|x");
    }

    @Test
    @DisplayName("AuthException の messageId が null なら既定の E0004")
    void authDefault() {
        assertBase(handler.handleAuth(new AuthException(null), request), 401, "E0004");
    }

    @Test
    @DisplayName("AuthenticationException は 401 E0004")
    void authentication() {
        assertBase(handler.handleAuthentication(new BadCredentialsException("bad"), request),
                401, "E0004");
    }

    @Test
    @DisplayName("AccessDeniedException は 403 E0002")
    void accessDenied() {
        assertBase(handler.handleAccessDenied(new AccessDeniedException("denied"), request), 403,
                "E0002");
    }

    @Test
    @DisplayName("EntityNotFoundException は 404 で既定 E0005 を返す")
    void entityNotFoundDefault() {
        assertBase(handler.handleEntityNotFound(new EntityNotFoundException(), request), 404,
                "E0005");
    }

    @Test
    @DisplayName("EntityNotFoundException は 404 で指定 messageId と args を返す")
    void entityNotFoundWithArgs() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(
                new EntityNotFoundException("E0099", "O1"), request);

        assertBase(response, 404, "E0099");
        assertThat(response.getBody().getMessageArgs()).containsExactly("O1");
    }

    @Test
    @DisplayName("BusinessException は 409 で messageId と args を返す")
    void business() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(new BusinessException("E0006", "a", "b"), request);

        assertBase(response, 409, "E0006");
        assertThat(response.getBody().getMessageArgs()).containsExactly("a", "b");
    }

    @Test
    @DisplayName("MaxUploadSizeExceededException は 413 E0010")
    void maxUploadSize() {
        assertBase(handler.handleMaxUploadSize(new MaxUploadSizeExceededException(1024), request),
                413, "E0010");
    }

    @Test
    @DisplayName("PayloadTooLargeException は 413 で messageId と args を返す")
    void payloadTooLarge() {
        ResponseEntity<ErrorResponse> response = handler.handlePayloadTooLarge(
                new PayloadTooLargeException("E0052", "50"), request);

        assertBase(response, 413, "E0052");
        assertThat(response.getBody().getMessageArgs()).containsExactly("50");
    }

    @Test
    @DisplayName("DataAccessException は 500 E0012")
    void dataAccess() {
        DataAccessException ex = new DataRetrievalFailureException("db down");

        assertBase(handler.handleDataAccessException(ex, request), 500, "E0012");
    }

    @Test
    @DisplayName("未捕捉の Exception は 500 E0012 で例外メッセージを返さない")
    void unexpected() {
        ResponseEntity<ErrorResponse> response =
                handler.handleException(new IllegalStateException("internal detail"), request);

        assertBase(response, 500, "E0012");
        assertThat(response.getBody().getDetails()).isNull();
        assertThat(response.getBody().getMessage()).doesNotContain("internal detail");
    }

    @Test
    @DisplayName("messageArgs は message の {0} 置換に使われる")
    void messageArgsSubstituted() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(new BusinessException("E0006", "a", "b"), request);

        assertThat(response.getBody().getMessage()).isEqualTo("msg:E0006|a,b");
    }

    @Test
    @DisplayName("カタログに無い messageId の場合 message は messageId 自体になる")
    void messageFallsBackToMessageId() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBusiness(new BusinessException("E9999"), request);

        assertThat(response.getBody().getMessage()).isEqualTo("E9999");
    }

    @Test
    @DisplayName("実カタログ（YamlMessageCatalog）では日本語正本が message に入る")
    void messageFromRealCatalog() {
        YamlMessageCatalog real = new YamlMessageCatalog();
        ReflectionTestUtils.invokeMethod(real, "init");
        GlobalExceptionHandler realHandler = new GlobalExceptionHandler(real);

        ResponseEntity<ErrorResponse> response =
                realHandler.handleEntityNotFound(new EntityNotFoundException(), request);

        assertThat(response.getBody().getMessage()).isEqualTo("対象が見つかりません");
    }

    private void assertRouteNotFound(Exception ex) {
        ResponseEntity<ErrorResponse> response = handler.handleRouteNotFound(ex, request);

        assertBase(response, 404, "E0005");
        assertThat(response.getBody().getDetails()).isNull();
        assertThat(response.getBody().getMessage()).doesNotContain("secret");
    }

    @Test
    @DisplayName("NoResourceFoundException は 404 E0005")
    void noResourceFound() {
        assertRouteNotFound(new NoResourceFoundException(
                org.springframework.http.HttpMethod.GET, "/secret/path"));
    }

    @Test
    @DisplayName("NoHandlerFoundException は 404 E0005")
    void noHandlerFound() {
        assertRouteNotFound(new NoHandlerFoundException("GET", "/secret/path",
                new org.springframework.http.HttpHeaders()));
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException は 404 E0005")
    void methodNotSupported() {
        assertRouteNotFound(new HttpRequestMethodNotSupportedException("secret"));
    }

    @Test
    @DisplayName("HttpMediaTypeNotSupportedException は 400 E0008")
    void mediaTypeNotSupported() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaType(
                new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN, List.of()), request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails()).isNull();
    }

    @Test
    @DisplayName("HttpMediaTypeNotAcceptableException は 400 E0008")
    void mediaTypeNotAcceptable() {
        ResponseEntity<ErrorResponse> response = handler.handleMediaType(
                new HttpMediaTypeNotAcceptableException(List.of(MediaType.APPLICATION_JSON)),
                request);

        assertBase(response, 400, "E0008");
        assertThat(response.getBody().getDetails()).isNull();
    }
}
