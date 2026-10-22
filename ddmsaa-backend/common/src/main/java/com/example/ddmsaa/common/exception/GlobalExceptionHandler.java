package com.example.ddmsaa.common.exception;

import com.example.ddmsaa.common.message.MessageCatalog;
import com.example.ddmsaa.common.response.ErrorResponse;
import com.example.ddmsaa.common.response.FieldError;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 例外を共通の {@link ErrorResponse} と HTTP ステータスへ変換するグローバルハンドラ。
 *
 * <p>例外の内部メッセージ・スタックトレースは利用者へ返さない。message には
 * {@link MessageCatalog} の日本語正本（取れなければ messageId 自体）を設定する。
 * 未登録の例外は 500 E0012 に集約する。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TRACE_ID_KEY = "traceId";

    private static final String E_INVALID_INPUT = "E0008";

    private static final String E_AUTH_FAILED = "E0004";

    private static final String E_FORBIDDEN = "E0002";

    private static final String E_NOT_FOUND = "E0005";

    private static final String E_FILE_TOO_LARGE = "E0010";

    private static final String E_SYSTEM = "E0012";

    /** 形式不正（E0031「形式が正しくありません」）に対応する理由。 */
    private static final String REASON_INVALID_FORMAT = "形式が正しくありません";

    /** messageId から日本語正本の文言を引くカタログ。 */
    private final MessageCatalog messageCatalog;

    /**
     * ハンドラを生成する。
     *
     * @param messageCatalog message フォールバック文言の解決に用いるカタログ
     */
    public GlobalExceptionHandler(MessageCatalog messageCatalog) {
        this.messageCatalog = messageCatalog;
    }

    /**
     * 検証付きボディの検証エラーを 400 E0008 に変換する。
     *
     * @param ex 検証エラー
     * @param request リクエスト
     * @return 400 と項目エラー付きの ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> details.add(new FieldError(e.getField(), e.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors().forEach(
                e -> details.add(new FieldError(e.getObjectName(), e.getDefaultMessage())));
        log.warn("Validation failed: path={}, traceId={}", request.getRequestURI(), traceId());
        ErrorResponse body = base(E_INVALID_INPUT, null, request);
        body.setDetails(details);
        return respond(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * パラメータ等の Bean Validation 違反を 400 E0008 に変換する。
     *
     * @param ex 制約違反
     * @param request リクエスト
     * @return 400 と項目エラー付きの ErrorResponse
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        List<FieldError> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            details.add(new FieldError(lastNode(violation.getPropertyPath()),
                    violation.getMessage()));
        }
        log.warn("Constraint violation: path={}, traceId={}", request.getRequestURI(),
                traceId());
        ErrorResponse body = base(E_INVALID_INPUT, null, request);
        body.setDetails(details);
        return respond(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * 業務側が投げた入力不正を 400 に変換する（messageId 未指定なら E0008）。
     *
     * @param ex 入力不正
     * @param request リクエスト
     * @return 400 と項目エラー付きの ErrorResponse
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex,
            HttpServletRequest request) {
        String messageId = ex.getMessageId() == null ? E_INVALID_INPUT : ex.getMessageId();
        log.warn("Invalid request: messageId={}, path={}, traceId={}", messageId,
                request.getRequestURI(), traceId());
        ErrorResponse body = base(messageId, null, request);
        body.setDetails(ex.getDetails());
        return respond(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * 型変換失敗・JSON 不正・必須パラメータ欠落を 400 E0008 に変換する。
     * パーサのメッセージは返さず、対象項目名と固定の理由のみを details に設定する。
     *
     * @param ex リクエスト束縛の例外
     * @param request リクエスト
     * @return 400 と項目エラー付きの ErrorResponse
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class, MissingPathVariableException.class})
    public ResponseEntity<ErrorResponse> handleRequestBinding(Exception ex,
            HttpServletRequest request) {
        log.warn("Request binding failed: type={}, path={}, traceId={}",
                ex.getClass().getSimpleName(), request.getRequestURI(), traceId());
        ErrorResponse body = base(E_INVALID_INPUT, null, request);
        body.setDetails(List.of(new FieldError(bindingField(ex), REASON_INVALID_FORMAT)));
        return respond(HttpStatus.BAD_REQUEST, body);
    }

    /**
     * 業務側が投げた認証失敗を 401 に変換する（messageId 未指定なら E0004）。
     *
     * @param ex 認証失敗
     * @param request リクエスト
     * @return 401 の ErrorResponse
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthException ex,
            HttpServletRequest request) {
        String messageId = ex.getMessageId() == null ? E_AUTH_FAILED : ex.getMessageId();
        log.warn("Authentication failed: messageId={}, path={}, traceId={}", messageId,
                request.getRequestURI(), traceId());
        return respond(HttpStatus.UNAUTHORIZED, base(messageId, ex.getArgs(), request));
    }

    /**
     * Spring Security の認証失敗を 401 E0004 に変換する。
     *
     * @param ex 認証失敗
     * @param request リクエスト
     * @return 401 の ErrorResponse
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
            HttpServletRequest request) {
        log.warn("Authentication failed: path={}, traceId={}", request.getRequestURI(),
                traceId());
        return respond(HttpStatus.UNAUTHORIZED, base(E_AUTH_FAILED, null, request));
    }

    /**
     * 権限不足を 403 E0002 に変換する。
     *
     * @param ex アクセス拒否
     * @param request リクエスト
     * @return 403 の ErrorResponse
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
            HttpServletRequest request) {
        log.warn("Access denied: path={}, traceId={}", request.getRequestURI(), traceId());
        return respond(HttpStatus.FORBIDDEN, base(E_FORBIDDEN, null, request));
    }

    /**
     * 対象リソース不在を 404 に変換する（messageId 未指定なら E0005）。
     *
     * @param ex 対象なし
     * @param request リクエスト
     * @return 404 の ErrorResponse
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex,
            HttpServletRequest request) {
        String messageId = ex.getMessageId() == null ? E_NOT_FOUND : ex.getMessageId();
        log.warn("Entity not found: messageId={}, path={}, traceId={}", messageId,
                request.getRequestURI(), traceId());
        return respond(HttpStatus.NOT_FOUND, base(messageId, ex.getArgs(), request));
    }

    /**
     * 存在しないパス・静的リソース・未対応メソッドを 404 E0005 に変換する。
     * 解析器のメッセージは返さない。
     *
     * @param ex フレームワークのパス/メソッド不一致例外
     * @param request リクエスト
     * @return 404 の ErrorResponse
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleRouteNotFound(Exception ex,
            HttpServletRequest request) {
        log.warn("Route not found: type={}, path={}, traceId={}",
                ex.getClass().getSimpleName(), request.getRequestURI(), traceId());
        return respond(HttpStatus.NOT_FOUND, base(E_NOT_FOUND, null, request));
    }

    /**
     * 未対応の Content-Type / Accept を 400 E0008 に変換する。解析器のメッセージは返さない。
     *
     * @param ex メディアタイプ不一致の例外
     * @param request リクエスト
     * @return 400 の ErrorResponse
     */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class})
    public ResponseEntity<ErrorResponse> handleMediaType(Exception ex,
            HttpServletRequest request) {
        log.warn("Media type rejected: type={}, path={}, traceId={}",
                ex.getClass().getSimpleName(), request.getRequestURI(), traceId());
        return respond(HttpStatus.BAD_REQUEST, base(E_INVALID_INPUT, null, request));
    }

    /**
     * 業務ルール違反を 409 に変換する。
     *
     * @param ex 業務ルール違反
     * @param request リクエスト
     * @return 409 の ErrorResponse
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
            HttpServletRequest request) {
        log.warn("Business rule violation: messageId={}, path={}, traceId={}",
                ex.getMessageId(), request.getRequestURI(), traceId());
        return respond(HttpStatus.CONFLICT, base(ex.getMessageId(), ex.getArgs(), request));
    }

    /**
     * multipart のアップロードサイズ超過を 413 E0010 に変換する。
     *
     * @param ex サイズ超過
     * @param request リクエスト
     * @return 413 の ErrorResponse
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        log.warn("Upload size exceeded: path={}, traceId={}", request.getRequestURI(),
                traceId());
        return respond(HttpStatus.PAYLOAD_TOO_LARGE, base(E_FILE_TOO_LARGE, null, request));
    }

    /**
     * 業務側が投げたサイズ超過を 413 に変換する。
     *
     * @param ex サイズ超過
     * @param request リクエスト
     * @return 413 の ErrorResponse
     */
    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<ErrorResponse> handlePayloadTooLarge(PayloadTooLargeException ex,
            HttpServletRequest request) {
        log.warn("Payload too large: messageId={}, path={}, traceId={}", ex.getMessageId(),
                request.getRequestURI(), traceId());
        return respond(HttpStatus.PAYLOAD_TOO_LARGE,
                base(ex.getMessageId(), ex.getArgs(), request));
    }

    /**
     * データアクセス例外を 500 E0012 に変換する（詳細はログのみ）。
     *
     * @param ex データアクセス例外
     * @param request リクエスト
     * @return 500 の ErrorResponse
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex,
            HttpServletRequest request) {
        log.error("Data access error: path={}, traceId={}", request.getRequestURI(), traceId(),
                ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, base(E_SYSTEM, null, request));
    }

    /**
     * 上記以外の未捕捉例外を 500 E0012 に変換する（詳細はログのみ）。
     *
     * @param ex 未捕捉例外
     * @param request リクエスト
     * @return 500 の ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex,
            HttpServletRequest request) {
        log.error("Unhandled exception: path={}, traceId={}", request.getRequestURI(), traceId(),
                ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, base(E_SYSTEM, null, request));
    }

    /** messageId・置換引数・フォールバック文言・path・traceId を設定した本体を作る。 */
    private ErrorResponse base(String messageId, String[] args, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(messageId, request.getRequestURI(), traceId());
        body.setMessageArgs(args);
        body.setMessage(messageCatalog.resolve(messageId, (Object[]) args));
        return body;
    }

    private static ResponseEntity<ErrorResponse> respond(HttpStatus status, ErrorResponse body) {
        return ResponseEntity.status(status).body(body);
    }

    private static String traceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    private static String lastNode(Path path) {
        String last = null;
        for (Path.Node node : path) {
            last = node.getName();
        }
        return last;
    }

    /** 形式不正の対象項目名（取れない場合は null。パーサのメッセージは使わない）。 */
    private static String bindingField(Exception ex) {
        if (ex instanceof MethodArgumentTypeMismatchException e) {
            return e.getName();
        }
        if (ex instanceof MissingServletRequestParameterException e) {
            return e.getParameterName();
        }
        if (ex instanceof MissingServletRequestPartException e) {
            return e.getRequestPartName();
        }
        if (ex instanceof MissingPathVariableException e) {
            return e.getVariableName();
        }
        if (ex instanceof HttpMessageNotReadableException e
                && e.getCause() instanceof JsonMappingException jme
                && !jme.getPath().isEmpty()) {
            return jme.getPath().get(jme.getPath().size() - 1).getFieldName();
        }
        return null;
    }
}
