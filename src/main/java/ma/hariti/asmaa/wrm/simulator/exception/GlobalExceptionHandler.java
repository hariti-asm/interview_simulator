package ma.hariti.asmaa.wrm.simulator.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import ma.hariti.asmaa.wrm.simulator.dto.response.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error("Validation failed", errors, HttpStatus.BAD_REQUEST.value()).getData());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleEntityNotFoundException(
            EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalStateException(
            IllegalStateException ex) {
        HttpStatus status;
        String message = ex.getMessage();
        if (message.contains("already exists")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("cannot be empty") || message.contains("is required")) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.error("IllegalStateException: {}", message);
        return ResponseEntity
                .status(status)
                .body(ApiResponseDTO.error(message, status.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}
