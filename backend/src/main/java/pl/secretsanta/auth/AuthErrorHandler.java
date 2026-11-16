package pl.secretsanta.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@RestControllerAdvice(assignableTypes = AuthController.class)
class AuthErrorHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthErrorHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldIssue> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldIssue(
                        "passwordWithinBcryptLimit".equals(error.getField()) ? "password" : error.getField(),
                        error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ValidationError(
                "VALIDATION_ERROR", "Sprawdź dane rejestracji.", fields));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Podaj poprawne dane w formacie JSON."));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ApiError> duplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("EMAIL_ALREADY_REGISTERED", "Konto z tym adresem e-mail już istnieje."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception) {
        log.error("Registration failed unexpectedly ({})", exception.getClass().getSimpleName());
        return ResponseEntity.internalServerError()
                .body(new ApiError("INTERNAL_ERROR", "Nie udało się zarejestrować konta."));
    }

    record ApiError(String code, String message) {
    }

    record ValidationError(String code, String message, List<FieldIssue> fields) {
    }

    record FieldIssue(String field, String message) {
    }
}
