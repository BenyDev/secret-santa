package pl.secretsanta.event;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import pl.secretsanta.event.dto.JoinRequest;

@RestControllerAdvice(assignableTypes = {GiftEventController.class, InvitationController.class})
class GiftEventErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ValidationError> invalidFields(MethodArgumentNotValidException exception) {
        List<FieldIssue> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldIssue(error.getField(), error.getDefaultMessage()))
                .toList();
        String message = exception.getParameter().getParameterType() == JoinRequest.class
                ? "Sprawdź dane uczestnika." : "Sprawdź dane wydarzenia.";
        return ResponseEntity.badRequest().body(new ValidationError("VALIDATION_ERROR", message, fields));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ValidationError> invalidJson() {
        return ResponseEntity.badRequest().body(new ValidationError(
                "INVALID_REQUEST", "Podaj poprawne dane wydarzenia w formacie JSON.", List.of()));
    }

    record ValidationError(String code, String message, List<FieldIssue> fields) {
    }

    record FieldIssue(String field, String message) {
    }
}
