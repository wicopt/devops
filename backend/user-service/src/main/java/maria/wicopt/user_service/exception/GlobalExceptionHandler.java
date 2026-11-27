package maria.wicopt.user_service.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public record FieldViolation(String field, String message) {}

    // 1. Наши бизнес-ошибки
    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApi(ApiException ex) {
        return problem(ex.getCode(), ex.getMessage());
    }

    // 2. Валидация тела запроса (@Valid @RequestBody)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldViolation(e.getField(), String.valueOf(e.getDefaultMessage())))
                .toList();

        ProblemDetail pd = problem(ErrorCode.VALIDATION_FAILED, "Request validation failed");
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    // 3. Валидация параметров (@Validated на контроллере: @PathVariable, @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldViolation> errors = ex.getConstraintViolations().stream()
                .map(v -> new FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        ProblemDetail pd = problem(ErrorCode.VALIDATION_FAILED, "Request validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    // 4. Нарушение ограничений БД (гонка двух запросов, обходящая проверку в сервисе)
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        String constraint = ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException cve
                ? cve.getConstraintName()
                : null;

        ErrorCode code = switch (constraint == null ? "" : constraint) {
            case "users_username_unique"   -> ErrorCode.USERNAME_TAKEN;
            case "users_email_unique"      -> ErrorCode.EMAIL_TAKEN;
            case "friendships_pair_unique" -> ErrorCode.FRIEND_REQUEST_EXISTS;
            default                        -> ErrorCode.CONFLICT;
        };

        if (code == ErrorCode.CONFLICT) {
            log.warn("Unmapped data integrity violation", ex);
        }
        return problem(code, code.getTitle());
    }

    // 5. Security-исключения (приходят и из фильтров через resolver, и из @PreAuthorize)
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getTitle());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getTitle());
    }

    // 6. Всё остальное: логируем, наружу детали не отдаём
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getTitle());
    }

    private ProblemDetail problem(ErrorCode code, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(code.getStatus(), detail);
        pd.setTitle(code.getTitle());
        pd.setProperty("code", code.name());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}