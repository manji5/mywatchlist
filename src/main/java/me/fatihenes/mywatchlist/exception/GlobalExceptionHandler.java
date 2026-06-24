package me.fatihenes.mywatchlist.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ResourceConflictException exception) {
        return new ResponseEntity<>(ApiErrorResponse.of(HttpStatus.CONFLICT.value(), "Conflict",
                exception.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return new ResponseEntity<>(ApiErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not found",
                exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnautherizedException(
            UnauthorizedAccessException exception) {
        return new ResponseEntity<>(ApiErrorResponse.of(HttpStatus.FORBIDDEN.value(),
                "Unautherized access", exception.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(RuntimeException exception) {
        return new ResponseEntity<>(
                ApiErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error", "An unexpected error occured on the server."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception) {
        return new ResponseEntity<>(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(),
                "Bad request", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalService(
            ExternalServiceException exception) {
        return new ResponseEntity<>(ApiErrorResponse.of(HttpStatus.BAD_GATEWAY.value(),
                "Bad gateway", exception.getMessage()), HttpStatus.BAD_GATEWAY);
    }

    // Intercepts DTO validation errors and returns a clean 400 Bad Request with field details
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException exception) {

        Map<String, String> fieldErrors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));


        return new ResponseEntity<>(ApiErrorResponse.ofValidation(fieldErrors),
                HttpStatus.BAD_REQUEST);
    }
}
